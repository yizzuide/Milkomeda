package com.github.yizzuide.milkomeda.universe.polyfill;

import com.github.yizzuide.milkomeda.hydrogen.interceptor.HydrogenMappedInterceptor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.OrderComparator;
import org.springframework.core.Ordered;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.handler.AbstractHandlerMapping;
import org.springframework.web.servlet.handler.MappedInterceptor;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
                        return 0;
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
    }

    /**
     * 获取Spring MVC内部请求处理器拦截器
     * @param handlerMapping    AbstractHandlerMapping实现类
     * @return 拦截器列表
     */
    @SuppressWarnings("unchecked")
    public static List<Map<String, Object>> getAdaptedInterceptors(AbstractHandlerMapping handlerMapping) {
        try {
            List<HandlerInterceptor> handlerInterceptors = (List<HandlerInterceptor>) adaptedInterceptorsField.get(handlerMapping);
            return handlerInterceptors.stream().map(interceptor -> {
                MappedInterceptor mappedInterceptor = null;
                Map<String, Object> map = new HashMap<>();
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
