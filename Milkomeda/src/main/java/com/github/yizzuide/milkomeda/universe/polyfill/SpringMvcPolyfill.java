/*
 * Copyright (c) 2021 yizzuide All rights Reserved.
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.github.yizzuide.milkomeda.universe.polyfill;

import com.github.yizzuide.milkomeda.hydrogen.interceptor.HydrogenMappedInterceptor;
import com.github.yizzuide.milkomeda.universe.context.ApplicationContextHolder;
import com.github.yizzuide.milkomeda.util.ReflectUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.core.OrderComparator;
import org.springframework.core.Ordered;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.method.ControllerAdviceBean;
import org.springframework.web.method.annotation.ExceptionHandlerMethodResolver;
import org.springframework.web.method.support.HandlerMethodReturnValueHandler;
import org.springframework.web.method.support.HandlerMethodReturnValueHandlerComposite;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.handler.AbstractHandlerMapping;
import org.springframework.web.servlet.handler.HandlerExceptionResolverComposite;
import org.springframework.web.servlet.handler.MappedInterceptor;
import org.springframework.web.servlet.mvc.method.annotation.AbstractMessageConverterMethodArgumentResolver;
import org.springframework.web.servlet.mvc.method.annotation.ExceptionHandlerExceptionResolver;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * SpringMvcPolyfill
 * Spring MVC功能扩展类
 *
 * @author yizzuide
 * @since 3.0.0
 * @version 3.3.0
 * <br>
 * Create at 2020/03/28 00:33
 */
@Slf4j
public class SpringMvcPolyfill {

    // 缓存反射字段
    private static Field adaptedInterceptorsField;

    /**
     * 动态添加异常切面
     * @param handlerExceptionResolver  HandlerExceptionResolver
     * @param beanFactory               BeanFactory
     * @param beanName                  异常拦截处理器bean名
     */
    public static void addDynamicExceptionAdvice(HandlerExceptionResolver handlerExceptionResolver, BeanFactory beanFactory, String beanName) {
        if (handlerExceptionResolver instanceof HandlerExceptionResolverComposite) {
            List<HandlerExceptionResolver> exceptionResolvers = ((HandlerExceptionResolverComposite) handlerExceptionResolver).getExceptionResolvers();
            for (HandlerExceptionResolver exceptionResolver : exceptionResolvers) {
                if (exceptionResolver instanceof ExceptionHandlerExceptionResolver) {
                    // TODO <mark> 由于使用底层API, 这个exceptionHandlerAdviceCache属性在未来版本中可能会改
                    Map<ControllerAdviceBean, ExceptionHandlerMethodResolver> resolverMap = ReflectUtil.invokeFieldPath(exceptionResolver, "exceptionHandlerAdviceCache");
                    if (resolverMap == null) {
                        resolverMap = new HashMap<>(2);
                    }
                    // 仿Spring MVC源码创建advice
                    ControllerAdviceBean adviceBean = new ControllerAdviceBean(beanName, ApplicationContextHolder.get(), null);
                    Class<?> beanType = adviceBean.getBeanType();
                    if (beanType == null) {
                        throw new IllegalStateException("SpringMvcPolyfill find unresolvable type for ControllerAdviceBean: " + adviceBean);
                    }
                    ExceptionHandlerMethodResolver resolver = new ExceptionHandlerMethodResolver(beanType);
                    if (resolver.hasExceptionMappings()) {
                        resolverMap.put(adviceBean, resolver);
                    }
                    break;
                }
            }
        }
    }

    /**
     * 动态添加消息体响应切面
     * @param returnValueHandlers       响应处理器列表（需要注入 {@link org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter#getReturnValueHandlers()}）
     * @param handlerExceptionResolver  异常处理解析器（需要注入 {@link org.springframework.web.servlet.HandlerExceptionResolver}）
     * @param responseBodyAdvice        ResponseBodyAdvice
     */
    public static void addDynamicResponseBodyAdvice(List<HandlerMethodReturnValueHandler> returnValueHandlers, HandlerExceptionResolver handlerExceptionResolver, ResponseBodyAdvice<?> responseBodyAdvice) {
        if (CollectionUtils.isEmpty(returnValueHandlers)) {
            return;
        }
        // 下面这行添加不起作用，由于内部构建已经完成
        // adapter.setResponseBodyAdvice(Collections.singletonList(responseBodyAdvice));
        for (HandlerMethodReturnValueHandler returnValueHandler : returnValueHandlers) {
            // 只有AbstractMessageConverterMethodArgumentResolver继承类型（主要是HttpEntityMethodProcessor、RequestResponseBodyMethodProcessor）有Advice Chain
            if (returnValueHandler instanceof AbstractMessageConverterMethodArgumentResolver) {
                // TODO <mark> 由于使用底层API, 这个advice.responseBodyAdvice属性在未来版本中可能会改
                List<Object> advices = ReflectUtil.invokeFieldPath(returnValueHandler, "advice.responseBodyAdvice");
                if (CollectionUtils.isEmpty(advices)) {
                    continue;
                }
                advices.add(responseBodyAdvice);
            }
        }

        // 动态添加到异常处理（因为源码流程中的异常处理是新加载的HandlerExceptionResolver，与正常响应处理不是同个处理集）
        if (handlerExceptionResolver instanceof HandlerExceptionResolverComposite) {
            // SpringMVC默认为注册HandlerExceptionResolverComposite的Bean
            List<HandlerExceptionResolver> exceptionResolvers = ((HandlerExceptionResolverComposite) handlerExceptionResolver).getExceptionResolvers();
            if (CollectionUtils.isEmpty(exceptionResolvers)) {
                return;
            }
            for (HandlerExceptionResolver exceptionResolver : exceptionResolvers) {
                if (exceptionResolver instanceof ExceptionHandlerExceptionResolver) {
                    HandlerMethodReturnValueHandlerComposite returnValueHandlerComposite = ((ExceptionHandlerExceptionResolver) exceptionResolver).getReturnValueHandlers();
                    if (returnValueHandlerComposite == null) {
                        return;
                    }
                    SpringMvcPolyfill.addDynamicResponseBodyAdvice(returnValueHandlerComposite.getHandlers(), null, responseBodyAdvice);
                }
            }
        }
    }

    /**
     * 动态添加拦截器
     * @param interceptor       拦截器
     * @param order             排序
     * @param includeURLs       需要拦截的URL
     * @param excludeURLs       排除拦截的URL
     * @param handlerMapping    AbstractHandlerMapping实现类
     */
    @SuppressWarnings("all")
    public static void addDynamicInterceptor(HandlerInterceptor interceptor, int order, List<String> includeURLs, List<String> excludeURLs, AbstractHandlerMapping handlerMapping) {
        String[] include = StringUtils.toStringArray(includeURLs);
        String[] exclude = StringUtils.toStringArray(excludeURLs);
        // HandlerInterceptor -> MappedInterceptor -> HydrogenMappedInterceptor
        HydrogenMappedInterceptor hmi = new HydrogenMappedInterceptor(new MappedInterceptor(include, exclude, interceptor));
        // 内部的处理流程会设置，然而不是最终采纳的拦截器列表
        // handlerMapping.setInterceptors(mappedInterceptor);
        hmi.setOrder(order);
        try {
            findAdaptedInterceptorsField(handlerMapping);
            // 添加到可采纳的拦截器列表，让拦截器处理器Chain流程获取得到这个拦截器
            List<HandlerInterceptor> handlerInterceptors = (List<HandlerInterceptor>) adaptedInterceptorsField.get(handlerMapping);
            // 过滤添加过的拦截器
            boolean mapped = handlerInterceptors.stream().anyMatch(itor -> {
                // 只判断HydrogenMappedInterceptor拦截器类型
                if (itor instanceof HydrogenMappedInterceptor) {
                    return itor.equals(hmi);
                }
                return false;
            });
            if (mapped) {
                return;
            }
            handlerInterceptors.add(hmi);
            // 仿Spring MVC源码对拦截器排序
            handlerInterceptors = handlerInterceptors.stream()
                    .sorted(OrderComparator.INSTANCE.withSourceProvider(itor -> {
                        if (itor instanceof HydrogenMappedInterceptor) {
                            return (Ordered) ((HydrogenMappedInterceptor) itor)::getOrder;
                        }
                        return null;
                    })).collect(Collectors.toList());
            adaptedInterceptorsField.set(handlerMapping, handlerInterceptors);
        } catch (Exception e) {
            log.error("SpringMvcPolyfill invoke AbstractHandlerMapping.adaptedInterceptors error with msg: {}",  e.getMessage(), e);
        }
    }

    /**
     * 动态删除拦截器
     * @param interceptor       HandlerInterceptor
     * @param handlerMapping    AbstractHandlerMapping实现类
     */
    @SuppressWarnings("all")
    public static void removeDynamicInterceptor(HandlerInterceptor interceptor, AbstractHandlerMapping handlerMapping) {
        if (interceptor == null) return;
        try {
            findAdaptedInterceptorsField(handlerMapping);
            List<HandlerInterceptor> handlerInterceptors = (List<HandlerInterceptor>) adaptedInterceptorsField.get(handlerMapping);
            List<HandlerInterceptor> shouldRemoveInterceptors = handlerInterceptors.stream().filter(itor -> {
                // 只判断映射的拦截器类型
                if (itor instanceof HydrogenMappedInterceptor) {
                    return itor.equals(interceptor);
                }
                return false;
            }).collect(Collectors.toList());
            handlerInterceptors.removeAll(shouldRemoveInterceptors);
            adaptedInterceptorsField.set(handlerMapping, handlerInterceptors);
        } catch (Exception e) {
            log.error("SpringMvcPolyfill invoke AbstractHandlerMapping.adaptedInterceptors error with msg: {}",  e.getMessage(), e);
        }
    }

    /**
     * 查找adaptedInterceptors
     * @param handlerMapping        AbstractHandlerMapping实现类
     * @throws NoSuchFieldException 反射字段异常
     */
    private static void findAdaptedInterceptorsField(AbstractHandlerMapping handlerMapping) throws NoSuchFieldException {
        if (adaptedInterceptorsField == null) {
            // 虽然用了反射，但这些代码只在启动时加载
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
    }

    /**
     * 获取Spring MVC内部请求处理器拦截器
     * @param handlerMapping    AbstractHandlerMapping实现类
     * @return 拦截器列表
     */
    @SuppressWarnings("unchecked")
    public static List<Map<String, String>> getInterceptorsInfo(AbstractHandlerMapping handlerMapping) {
        try {
            List<HandlerInterceptor> handlerInterceptors = (List<HandlerInterceptor>) adaptedInterceptorsField.get(handlerMapping);
            return handlerInterceptors.stream().map(interceptor -> {
                MappedInterceptor mappedInterceptor = null;
                Map<String, String> map = new HashMap<>();
                String clazz = "";
                String include = "[/**]";
                String exclude = "[]";
                String order = "unknown";
                if (interceptor instanceof HydrogenMappedInterceptor) {
                    mappedInterceptor = ((HydrogenMappedInterceptor) interceptor).getMappedInterceptor();
                    order = String.valueOf(((HydrogenMappedInterceptor) interceptor).getOrder());
                } else if (interceptor instanceof MappedInterceptor) {
                    mappedInterceptor = (MappedInterceptor) interceptor;
                } else { // HandlerInterceptorAdapter
                    clazz = interceptor.getClass().getName();
                }
                if (mappedInterceptor != null) {
                    clazz = mappedInterceptor.getInterceptor().getClass().getName();
                    include = Arrays.toString(mappedInterceptor.getPathPatterns());
                    try {
                        // TODO <mark> 由于使用底层API, 这个MappedInterceptor.excludePatterns很后的版本可能会改
                        Field excludePatternsField = mappedInterceptor.getClass().getDeclaredField("excludePatterns");
                        excludePatternsField.setAccessible(true);
                        exclude = Arrays.toString((String[]) excludePatternsField.get(mappedInterceptor));
                    } catch (Exception e) {
                        log.error("SpringMvcPolyfill invoke get error with msg: {}", e.getMessage(), e);
                    }
                }
                map.put("class", clazz);
                map.put("include", include);
                map.put("exclude", exclude);
                map.put("order", order);
                return map;
            }).collect(Collectors.toList());
        } catch (Exception e) {
            log.error("SpringMvcPolyfill invoke get error with msg: {}", e.getMessage(), e);
            return null;
        }
    }
}
