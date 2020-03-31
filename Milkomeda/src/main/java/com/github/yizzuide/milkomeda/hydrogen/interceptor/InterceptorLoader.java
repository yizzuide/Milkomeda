package com.github.yizzuide.milkomeda.hydrogen.interceptor;

import com.github.yizzuide.milkomeda.hydrogen.core.HydrogenHolder;
import com.github.yizzuide.milkomeda.hydrogen.core.HydrogenProperties;
import com.github.yizzuide.milkomeda.universe.context.WebContext;
import com.github.yizzuide.milkomeda.universe.polyfill.SpringMvcPolyfill;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.cloud.context.environment.EnvironmentChangeEvent;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.event.EventListener;
import org.springframework.util.CollectionUtils;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

/**
 * InterceptorLoader
 * 拦截器加载器
 *
 * @author yizzuide
 * @since 3.0.0
 * Create at 2020/03/31 00:12
 */
@Slf4j
public class InterceptorLoader implements ApplicationContextAware {
    /**
     * 请求映射处理器
     */
    private RequestMappingHandlerMapping requestMappingHandlerMapping;
    
    private ApplicationContext applicationContext;

    /**
     * 加载完成的拦截器配置
     */
    private List<HydrogenProperties.Interceptor> loadedInterceptors;

    public InterceptorLoader(RequestMappingHandlerMapping requestMappingHandlerMapping) {
        this.requestMappingHandlerMapping = requestMappingHandlerMapping;
        this.loadedInterceptors = new ArrayList<>();
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
        // 刷新拦截器
        refresh();
    }

    /**
     * 加载拦截器
     * @param clazz     HandlerInterceptor实现类
     * @param include   拦截的URL
     * @param exclude   排除的URL
     * @param order     排序
     */
    public void load(@NonNull Class<? extends HandlerInterceptor> clazz, List<String> include, List<String> exclude, int order) {
        HydrogenProperties.Interceptor hInterceptor = new HydrogenProperties.Interceptor();
        hInterceptor.setClazz(clazz);
        if (include != null) {
            hInterceptor.setIncludeURLs(include);
        }
        hInterceptor.setExcludeURLs(exclude);
        hInterceptor.setOrder(order);
        transform(Collections.singletonList(hInterceptor), (hi, handlerInterceptor)  -> SpringMvcPolyfill.addDynamicInterceptor(handlerInterceptor,
                hInterceptor.getOrder(), hi.getIncludeURLs(), hi.getExcludeURLs(), this.requestMappingHandlerMapping));
    }

    /**
     * 卸载拦截器
     * @param clazz     HandlerInterceptor实现类
     */
    public void unLoad(@NonNull Class<? extends HandlerInterceptor> clazz) {
        HydrogenProperties.Interceptor hInterceptor = new HydrogenProperties.Interceptor();
        hInterceptor.setClazz(clazz);
        transform(Collections.singletonList(hInterceptor), (hi, handlerInterceptor)  ->
                SpringMvcPolyfill.removeDynamicInterceptor(handlerInterceptor, this.requestMappingHandlerMapping));
    }

    /**
     * 探查拦截器信息，包含：class、include、exclude、order
     * @return  拦截器列表信息
     */
    public List<Map<String, Object>> inspect() {
        return SpringMvcPolyfill.getAdaptedInterceptors(this.requestMappingHandlerMapping);
    }

    @EventListener
    public void configListener(EnvironmentChangeEvent event) {
        // 键没有修改，直接返回
        if (CollectionUtils.isEmpty(event.getKeys())) {
            return;
        }
        refresh();
    }

    /**
     * 从配置加载拦截器
     */
    private void refresh() {
        // 刷新配置后的拦截器列表
        List<HydrogenProperties.Interceptor> afterInterceptors = HydrogenHolder.getProps().getInterceptors();
        // 删除加载过的拦截器
        transform(this.loadedInterceptors, (hi, handlerInterceptor)  ->
                SpringMvcPolyfill.removeDynamicInterceptor(handlerInterceptor, this.requestMappingHandlerMapping));
        // 加载新配置的拦截器
        transform(afterInterceptors, (hi, handlerInterceptor)  -> SpringMvcPolyfill.addDynamicInterceptor(handlerInterceptor,
                hi.getOrder(), hi.getIncludeURLs(), hi.getExcludeURLs(), this.requestMappingHandlerMapping));
        this.loadedInterceptors = afterInterceptors;
    }

    private void transform(List<HydrogenProperties.Interceptor> hydrogenInterceptors, @NonNull BiConsumer<HydrogenProperties.Interceptor, HandlerInterceptor> performAction) {
        if (CollectionUtils.isEmpty(hydrogenInterceptors)) {
            return;
        }
        hydrogenInterceptors.forEach(hi -> {
            HandlerInterceptor handlerInterceptorBean = null;
            try {
                handlerInterceptorBean = this.applicationContext.getBean(hi.getClazz());
            } catch (BeansException ignore) {
            }
            if (handlerInterceptorBean == null) {
                // 动态注册到IoC
                handlerInterceptorBean = WebContext.registerBean((ConfigurableApplicationContext) this.applicationContext, hi.getClazz().getSimpleName(), hi.getClazz());
                // 动态注入属性
                this.applicationContext.getAutowireCapableBeanFactory().autowireBean(handlerInterceptorBean);
            }
            try {
                // set props
                Map<String, Object> props = hi.getProps();
                for (Map.Entry<String, Object> entry : props.entrySet()) {
                    Field declaredField = handlerInterceptorBean.getClass().getDeclaredField(entry.getKey());
                    declaredField.setAccessible(true);
                    declaredField.set(handlerInterceptorBean, entry.getValue());
                }
                performAction.accept(hi, handlerInterceptorBean);
            } catch (Exception e) {
                log.error("Hydrogen interceptor add error with msg: {}", e.getMessage(), e);
                return;
            }
            // 记录已加载拦截器
            this.loadedInterceptors.add(hi);
        });
    }
}
