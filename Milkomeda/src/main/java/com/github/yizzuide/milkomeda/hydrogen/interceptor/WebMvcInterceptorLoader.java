package com.github.yizzuide.milkomeda.hydrogen.interceptor;

import com.github.yizzuide.milkomeda.universe.context.WebContext;
import com.github.yizzuide.milkomeda.universe.polyfill.SpringMvcPolyfill;
import com.github.yizzuide.milkomeda.util.ReflectUtil;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.util.CollectionUtils;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

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
public class WebMvcInterceptorLoader extends AbstractInterceptorLoader<InterceptorProperties.Interceptors> {
    
    /**
     * 请求映射处理器
     */
    private final RequestMappingHandlerMapping requestMappingHandlerMapping;
    /**
     * 配置
     */
    private final InterceptorProperties props;

    public WebMvcInterceptorLoader(InterceptorProperties interceptorProperties, RequestMappingHandlerMapping requestMappingHandlerMapping) {
        this.props = interceptorProperties;
        this.requestMappingHandlerMapping = requestMappingHandlerMapping;
    }
    
    @Override
    public void load(@NonNull Class<?> clazz, List<String> include, List<String> exclude, int order, Map<String, Object> props) {
        InterceptorProperties.Interceptors hInterceptor = new InterceptorProperties.Interceptors();
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
        InterceptorProperties.Interceptors hInterceptor = new InterceptorProperties.Interceptors();
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
        List<InterceptorProperties.Interceptors> afterInterceptors = this.props.getInterceptors();
        merge(afterInterceptors, i -> this.unLoad(i.getClazz()), i ->
                this.load(i.getClazz(), i.getIncludeURLs(), i.getExcludeURLs(), i.getOrder(), i.getProps()));
    }

    private void transform(List<InterceptorProperties.Interceptors> hydrogenInterceptors, @NonNull BiConsumer<InterceptorProperties.Interceptors, HandlerInterceptor> performAction) {
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
                ReflectUtil.setField(handlerInterceptorBean, hi.getProps());
                performAction.accept(hi, handlerInterceptorBean);
            } catch (Exception e) {
                log.error("Hydrogen interceptor add error with msg: {}", e.getMessage(), e);
            }
        });
    }
}
