package com.github.yizzuide.milkomeda.hydrogen.interceptor;

import com.github.yizzuide.milkomeda.hydrogen.core.HydrogenHolder;
import com.github.yizzuide.milkomeda.hydrogen.core.HydrogenProperties;
import com.github.yizzuide.milkomeda.universe.context.WebContext;
import com.github.yizzuide.milkomeda.universe.polyfill.SpringMvcPolyfill;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.util.CollectionUtils;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

/**
 * WebMvcInterceptorLoader
 * 拦截器加载器
 *
 * @author yizzuide
 * @since 3.0.0
 * Create at 2020/03/31 00:12
 */
@Slf4j
public class WebMvcInterceptorLoader extends AbstractInterceptorLoader<HydrogenProperties.Interceptors> {

    /**
     * 请求映射处理器
     */
    private RequestMappingHandlerMapping requestMappingHandlerMapping;

    public WebMvcInterceptorLoader(RequestMappingHandlerMapping requestMappingHandlerMapping) {
        this.requestMappingHandlerMapping = requestMappingHandlerMapping;
    }
    
    @Override
    public void load(@NonNull Class<?> clazz, List<String> include, List<String> exclude, int order, Map<String, Object> props) {
        HydrogenProperties.Interceptors hInterceptor = new HydrogenProperties.Interceptors();
        hInterceptor.setClazz(clazz);
        if (include != null) {
            hInterceptor.setIncludeURLs(include);
        }
        hInterceptor.setExcludeURLs(exclude);
        hInterceptor.setOrder(order);
        if (props != null) {
            hInterceptor.setProps(props);
        }
        transform(Collections.singletonList(hInterceptor), (hi, handlerInterceptor)  -> SpringMvcPolyfill.addDynamicInterceptor(handlerInterceptor,
                hInterceptor.getOrder(), hi.getIncludeURLs(), hi.getExcludeURLs(), this.requestMappingHandlerMapping));
    }
    
    @Override
    public void unLoad(@NonNull Class<?> clazz) {
        HydrogenProperties.Interceptors hInterceptor = new HydrogenProperties.Interceptors();
        hInterceptor.setClazz(clazz);
        transform(Collections.singletonList(hInterceptor), (hi, handlerInterceptor)  ->
                SpringMvcPolyfill.removeDynamicInterceptor(handlerInterceptor, this.requestMappingHandlerMapping));
    }
    
    @Override
    public List<Map<String, String>> inspect() {
        return SpringMvcPolyfill.getInterceptorsInfo(this.requestMappingHandlerMapping);
    }
    
    @Override
    public void refresh() {
        // 刷新配置后的拦截器列表
        List<HydrogenProperties.Interceptors> afterInterceptors = HydrogenHolder.getProps().getInterceptor().getInterceptors();
        merge(afterInterceptors, i -> this.unLoad(i.getClazz()), i ->
                this.load(i.getClazz(), i.getIncludeURLs(), i.getExcludeURLs(), i.getOrder(), i.getProps()));
    }

    private void transform(List<HydrogenProperties.Interceptors> hydrogenInterceptors, @NonNull BiConsumer<HydrogenProperties.Interceptors, HandlerInterceptor> performAction) {
        if (CollectionUtils.isEmpty(hydrogenInterceptors)) {
            return;
        }
        hydrogenInterceptors.forEach(hi -> {
            HandlerInterceptor handlerInterceptorBean = null;
            try {
                handlerInterceptorBean = (HandlerInterceptor) getApplicationContext().getBean(hi.getClazz());
            } catch (BeansException ignore) {
                // Just ignore this exception!
            }
            if (handlerInterceptorBean == null) {
                // 动态注册到IoC
                handlerInterceptorBean = (HandlerInterceptor) WebContext.registerBean((ConfigurableApplicationContext) getApplicationContext(), hi.getClazz().getSimpleName(), hi.getClazz());
                // 动态注入属性
                getApplicationContext().getAutowireCapableBeanFactory().autowireBean(handlerInterceptorBean);
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
            }
        });
    }
}
