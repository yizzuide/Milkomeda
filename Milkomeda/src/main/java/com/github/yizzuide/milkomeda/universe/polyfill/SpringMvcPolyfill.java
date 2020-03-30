package com.github.yizzuide.milkomeda.universe.polyfill;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.handler.AbstractHandlerMapping;
import org.springframework.web.servlet.handler.MappedInterceptor;

import java.lang.reflect.Field;
import java.util.List;

/**
 * SpringMvcPolyfill
 * Spring MVC功能填充类
 *
 * @author yizzuide
 * @since 3.0.0
 * Create at 2020/03/28 00:33
 */
@Slf4j
public class SpringMvcPolyfill {
    // 缓存反射字段
    private static Field adaptedInterceptorsField;

    /**
     * 动态添加拦截器
     * @param interceptor       拦截器
     * @param order             排序，目前仅支持Ordered.HIGHEST_PRECEDENCE
     * @param includeURLs       需要拦截的URL
     * @param excludeURLs       排除拦截的URL
     * @param handlerMapping    AbstractHandlerMapping实现类
     */
    @SuppressWarnings("all")
    public static void addDynamicInterceptor(HandlerInterceptor interceptor, Integer order, List<String> includeURLs, List<String> excludeURLs, AbstractHandlerMapping handlerMapping) {
        String[] include = StringUtils.toStringArray(includeURLs);
        String[] exclude = StringUtils.toStringArray(excludeURLs);
        // Interceptor -> MappedInterceptor
        MappedInterceptor mappedInterceptor = new MappedInterceptor(include, exclude, interceptor);
        // 下面这行可以省略，但为了保持内部的处理流程，使表达式成立：interceptors.count() == adaptedInterceptors.count()
        handlerMapping.setInterceptors(mappedInterceptor);
        try {
            if (adaptedInterceptorsField == null) {
                // 虽然用了反射，但这些代码在只在启动时加载
                // 查找继承链
                // RequestMappingHandlerMapping -> RequestMappingInfoHandlerMapping -> AbstractHandlerMethodMapping -> AbstractHandlerMapping
                // WelcomePageHandlerMapping -> AbstractUrlHandlerMapping -> AbstractHandlerMapping
                Class<?> abstractHandlerMapping = handlerMapping.getClass();
                while (abstractHandlerMapping != AbstractHandlerMapping.class) {
                    abstractHandlerMapping = abstractHandlerMapping.getSuperclass();
                }
                // TODO <mark> 由于使用底层API, 这个AbstractHandlerMapping.adaptedInterceptors很后的版本可能会改
                adaptedInterceptorsField = abstractHandlerMapping.getDeclaredField("adaptedInterceptors");
                adaptedInterceptorsField.setAccessible(true);
            }
            // 添加到可采纳的拦截器列表，让拦截器处理器Chain流程获取得到这个拦截器
            List<HandlerInterceptor> handlerInterceptors = (List<HandlerInterceptor>) adaptedInterceptorsField.get(handlerMapping);
            if (order != null) {
                if (order == Ordered.HIGHEST_PRECEDENCE) {
                    handlerInterceptors.add(0, mappedInterceptor);
                } else if (order > Ordered.HIGHEST_PRECEDENCE && order < 0){
                    handlerInterceptors.add(1, mappedInterceptor);
                } else {
                    handlerInterceptors.add(mappedInterceptor);
                }
            } else {
                handlerInterceptors.add(mappedInterceptor);
            }
            adaptedInterceptorsField.set(handlerMapping, handlerInterceptors);
        } catch (Exception e) {
            log.error("SpringMvcPolyfill invoke AbstractHandlerMapping.adaptedInterceptors error with msg: {}",  e.getMessage(), e);
        }
    }
}
