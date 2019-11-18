package com.github.yizzuide.milkomeda.universe.context;

import com.github.yizzuide.milkomeda.comet.CometAspect;
import com.github.yizzuide.milkomeda.comet.WebCometData;
import com.github.yizzuide.milkomeda.comet.XCometData;
import com.github.yizzuide.milkomeda.universe.metadata.HandlerMetaData;
import org.springframework.aop.framework.AopContext;
import org.springframework.aop.support.AopUtils;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.StringUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * AopContextHolder
 *
 * @author yizzuide
 * @since 1.13.4
 * @version 1.15.0
 * Create at 2019/10/24 21:17
 */
public class AopContextHolder {
    /**
     * 获得当前切面代理对象
     * <br>使用前通过<code>@EnableAspectJAutoProxy(proxyTargetClass = true, exposeProxy = true)</code>开启代理曝露
     *
     * @param clazz 当前类
     * @param <T>   当前类型
     * @return  代理对象
     */
    @SuppressWarnings("unchecked")
    public static <T> T self(Class<T> clazz) {
        return  (T)AopContext.currentProxy();
    }

    /**
     * 获取控制层采集数据
     *
     * @return WebCometData
     */
    public static WebCometData getWebCometData() {
        return CometAspect.getCurrentWebCometData();
    }

    /**
     * 获取服务层采集数据
     *
     * @return XCometData
     */
    public static XCometData getXCometData() {
        return CometAspect.getCurrentXCometData();
    }

    /**
     * 获取处理组件元数据
     * @param handlerAnnotationClazz    处理器注解类
     * @param executeAnnotationClazz    执行方法注解类
     * @param nameProvider              标识名称提供函数
     * @param useAOP                    执行方法是否使用了切面
     * @param onlyOneExecutorPerHandler 一个组件只有一个处理方法是传true
     * @return  Map
     */
    public static Map<String, List<HandlerMetaData>> getHandlerMetaData(
            Class<? extends Annotation> handlerAnnotationClazz,
            Class<? extends Annotation> executeAnnotationClazz,
            Function<Annotation, String> nameProvider,
            boolean useAOP,
            boolean onlyOneExecutorPerHandler) {
        Map<String, List<HandlerMetaData>> handlerMap = new HashMap<>();
        Map<String, Object> beanMap = ApplicationContextHolder.get().getBeansWithAnnotation(handlerAnnotationClazz);
        for (String key : beanMap.keySet()) {
            // 如果方法有aop切面，可以通过AopUtils.getTargetClass()获取
            Method[] methods = ReflectionUtils.getAllDeclaredMethods(useAOP ?
                    AopUtils.getTargetClass(beanMap.get(key).getClass()) : beanMap.get(key).getClass());
            for (Method method : methods) {
                // 获取指定方法上的注解的属性
                final Annotation executeAnnotation = AnnotationUtils.findAnnotation(method, executeAnnotationClazz);
                if (null == executeAnnotation) {
                    continue;
                }
                String name = nameProvider.apply(executeAnnotation);
                if (StringUtils.isEmpty(name)) {
                    throw new IllegalArgumentException("Please specify the [name] of "+ executeAnnotation +" !");
                }
                if (handlerMap.containsKey(name)) {
                    handlerMap.get(name).add(new HandlerMetaData(name, beanMap.get(key), method));
                } else {
                    List<HandlerMetaData> list = new ArrayList<>();
                    list.add(new HandlerMetaData(name, beanMap.get(key), method));
                    handlerMap.put(name, list);
                }
                // 如果一个组件只会一个处理方法，直接返回
                if (onlyOneExecutorPerHandler) {
                    break;
                }
            }
        }
        return handlerMap;
    }
}
