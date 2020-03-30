package com.github.yizzuide.milkomeda.hydrogen.interceptor;

import com.github.yizzuide.milkomeda.hydrogen.core.HydrogenHolder;
import com.github.yizzuide.milkomeda.hydrogen.core.HydrogenProperties;
import com.github.yizzuide.milkomeda.universe.context.ApplicationContextHolder;
import com.github.yizzuide.milkomeda.universe.context.WebContext;
import com.github.yizzuide.milkomeda.universe.polyfill.SpringMvcPolyfill;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.cloud.context.environment.EnvironmentChangeEvent;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.event.EventListener;
import org.springframework.core.OrderComparator;
import org.springframework.core.Ordered;
import org.springframework.util.CollectionUtils;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * InterceptorHandler
 *
 * @author yizzuide
 * @since 3.0.0
 * Create at 2020/03/31 00:12
 */
@Slf4j
public class InterceptorHandler {
    /**
     * 请求映射处理器
     */
    private RequestMappingHandlerMapping requestMappingHandlerMapping;

    /**
     * 加载完成的拦截器配置
     */
    private List<HydrogenProperties.Interceptor> loadedHandlerInterceptors;

    public InterceptorHandler(RequestMappingHandlerMapping requestMappingHandlerMapping) {
        this.requestMappingHandlerMapping = requestMappingHandlerMapping;
        this.loadedHandlerInterceptors = new ArrayList<>();
        load();
    }

    /**
     * 加载拦截器
     */
    public void load() {
        List<HydrogenProperties.Interceptor> hydrogenInterceptors = HydrogenHolder.getProps().getInterceptors();
        if (CollectionUtils.isEmpty(hydrogenInterceptors)) {
            return;
        }
        // 仿Spring MVC源码对拦截器排序
        if (hydrogenInterceptors.size() > 1) {
            hydrogenInterceptors = hydrogenInterceptors.stream()
                    .sorted(OrderComparator.INSTANCE.withSourceProvider(object -> {
                        if (object instanceof HydrogenProperties.Interceptor) {
                            return (Ordered) ((HydrogenProperties.Interceptor) object)::getOrder;
                        }
                        return null;
                    })).collect(Collectors.toList());
        }
        hydrogenInterceptors.forEach(hi -> {
            HandlerInterceptor handlerInterceptorBean = null;
            try {
                handlerInterceptorBean = ApplicationContextHolder.get().getBean(hi.getClazz());
            } catch (BeansException ignore) {
            }
            if (handlerInterceptorBean == null) {
                // 动态注册到IoC
                handlerInterceptorBean = WebContext.registerBean((ConfigurableApplicationContext) ApplicationContextHolder.get(), hi.getClazz().getSimpleName(), hi.getClazz());
                // 动态注入属性
                ApplicationContextHolder.get().getAutowireCapableBeanFactory().autowireBean(handlerInterceptorBean);
            }
            try {
                // set props
                Map<String, Object> props = hi.getProps();
                for (Map.Entry<String, Object> entry : props.entrySet()) {
                    Field declaredField = handlerInterceptorBean.getClass().getDeclaredField(entry.getKey());
                    declaredField.setAccessible(true);
                    declaredField.set(handlerInterceptorBean, entry.getValue());
                }
                SpringMvcPolyfill.addDynamicInterceptor(handlerInterceptorBean, null, hi.getIncludeURLs(), hi.getExcludeURLs(), this.requestMappingHandlerMapping);
            } catch (Exception e) {
                log.error("Hydrogen interceptor add error with msg: {}", e.getMessage(), e);
                return;
            }
            // 记录已加载拦截器
            this.loadedHandlerInterceptors.add(hi);
        });
    }

    @EventListener
    public void configListener(EnvironmentChangeEvent event) {
        // 键没有修改，直接返回
        /*if (CollectionUtils.isEmpty(event.getKeys())) {
            return;
        }
        if (event.getSource() instanceof AnnotationConfigServletWebServerApplicationContext) {
            HydrogenHolder.getProps().getInterceptors();
        }*/
    }
}
