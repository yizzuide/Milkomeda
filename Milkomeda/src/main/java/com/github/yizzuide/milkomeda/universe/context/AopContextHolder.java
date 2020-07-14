package com.github.yizzuide.milkomeda.universe.context;

import com.github.yizzuide.milkomeda.comet.core.CometAspect;
import com.github.yizzuide.milkomeda.comet.core.CometInterceptor;
import com.github.yizzuide.milkomeda.comet.core.WebCometData;
import com.github.yizzuide.milkomeda.comet.core.XCometData;
import com.github.yizzuide.milkomeda.universe.el.ELContext;
import com.github.yizzuide.milkomeda.universe.function.TripleFunction;
import com.github.yizzuide.milkomeda.universe.metadata.HandlerMetaData;
import org.springframework.aop.framework.AopContext;
import org.springframework.aop.support.AopUtils;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.util.ReflectionUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * AopContextHolder
 *
 * @author yizzuide
 * @since 1.13.4
 * @version 3.11.1
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
        // Filter层采集
        WebCometData webCometData = CometInterceptor.getWebCometData();
        if (webCometData == null) {
            // 方法注解采集
            return CometAspect.getCurrentWebCometData();
        }
        return webCometData;
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
     * @param onlyOneExecutorPerHandler 一个组件只有一个处理方法是传true
     * @return  Map
     */
    public static Map<String, List<HandlerMetaData>> getHandlerMetaData(
            Class<? extends Annotation> handlerAnnotationClazz,
            Class<? extends Annotation> executeAnnotationClazz,
            TripleFunction<Annotation, Annotation, HandlerMetaData, String> nameProvider,
            boolean onlyOneExecutorPerHandler) {
        Map<String, List<HandlerMetaData>> handlerMap = new HashMap<>();
        Map<String, Object> beanMap = ApplicationContextHolder.get().getBeansWithAnnotation(handlerAnnotationClazz);
        for (String key : beanMap.keySet()) {
            Object target = beanMap.get(key);
            // 查找AOP切面（通过Proxy.isProxyClass()判断类是否是代理的接口类，AopUtils.isAopProxy()判断对象是否被代理），可以通过AopUtils.getTargetClass()获取原Class
            Method[] methods = ReflectionUtils.getAllDeclaredMethods(AopUtils.isAopProxy(target) ?
                    AopUtils.getTargetClass(target) : target.getClass());
            Annotation handlerAnnotation = target.getClass().getAnnotation(handlerAnnotationClazz);
            for (Method method : methods) {
                // 获取指定方法上的注解的属性
                final Annotation executeAnnotation = AnnotationUtils.findAnnotation(method, executeAnnotationClazz);
                if (null == executeAnnotation) {
                    continue;
                }
                HandlerMetaData metaData = new HandlerMetaData();
                metaData.setTarget(target);
                metaData.setMethod(method);
                // 支持SpEL
                String name = nameProvider.apply(executeAnnotation, handlerAnnotation, metaData);
                if (name.startsWith("'") || name.startsWith("@") || name.startsWith("#") || name.startsWith("T(") || name.startsWith("args[")) {
                    name = ELContext.getValue(target, new Object[]{}, target.getClass(), method, name, String.class);
                }
                if (name == null) {
                    throw new IllegalArgumentException("Please specify the [tag] of "+ executeAnnotation +" !");
                }
                metaData.setName(name);
                if (handlerMap.containsKey(name)) {
                    handlerMap.get(name).add(metaData);
                } else {
                    List<HandlerMetaData> list = new ArrayList<>();
                    list.add(metaData);
                    handlerMap.put(name, list);
                }
                // 如果一个组件只会有一个处理方法，直接返回
                if (onlyOneExecutorPerHandler) {
                    break;
                }
            }
        }
        return handlerMap;
    }

    /**
     * 获取实现了指定接口的处理器
     * @param handlerAnnotationClazz    处理注解
     * @param <T>                       接口类型
     * @return  处理器列表
     * @since 3.3.0
     */
    @SuppressWarnings("unchecked")
    public static <T> List<T> getTypeHandlers(Class<? extends Annotation> handlerAnnotationClazz) {
        List<T> handlerMap = new ArrayList<>();
        Map<String, Object> beanMap = ApplicationContextHolder.get().getBeansWithAnnotation(handlerAnnotationClazz);
        for (String key : beanMap.keySet()) {
            T target = (T) beanMap.get(key);
            handlerMap.add(target);
        }
        return handlerMap;
    }
}
