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
 * @version 3.12.10
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
        return getWebCometData(true);
    }

    /**
     * 获取控制层采集数据
     * @param useTag    是否采用的TagCollector方式
     * @return  WebCometData
     * @since 3.12.10
     */
    public static WebCometData getWebCometData(boolean useTag) {
        // 方法注解采集（注解方式）
        if (!useTag) {
            return CometAspect.getCurrentWebCometData();
        }
        // 下面保留之前的逻辑
        // 拦截器层采集（用于TagCollector)
        WebCometData webCometData = CometInterceptor.getWebCometData();
        if (webCometData == null) {
            // 方法注解采集（注解方式）
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
            Class<?> targetClass = AopUtils.isAopProxy(target) ?
                    AopUtils.getTargetClass(target) : target.getClass();
            Method[] methods = ReflectionUtils.getAllDeclaredMethods(targetClass);
            Method[] wrapMethods = null;
            if (AopUtils.isAopProxy(target)) {
                wrapMethods = ReflectionUtils.getAllDeclaredMethods(target.getClass());
            }
            Annotation handlerAnnotation = targetClass.getAnnotation(handlerAnnotationClazz);
            for (Method method : methods) {
                // 获取指定方法上的注解的属性
                final Annotation executeAnnotation = AnnotationUtils.findAnnotation(method, executeAnnotationClazz);
                if (null == executeAnnotation) {
                    continue;
                }
                HandlerMetaData metaData = new HandlerMetaData();
                metaData.setTarget(target);
                if (wrapMethods == null) {
                    metaData.setMethod(method);
                } else {
                    for (Method wrapMethod : wrapMethods) {
                        if (method.getName().equals(wrapMethod.getName())) {
                            metaData.setMethod(wrapMethod);
                            break;
                        }
                    }
                }
                // 支持SpEL
                String name = nameProvider.apply(executeAnnotation, handlerAnnotation, metaData);
                if (name.startsWith("'") || name.startsWith("@") || name.startsWith("#") || name.startsWith("T(") || name.startsWith("args[")) {
                    name = ELContext.getValue(target, new Object[]{}, target.getClass(), method, name, String.class);
                }
                if (name == null) {
                    throw new IllegalArgumentException("Please specify the [tag] of "+ executeAnnotation +" !");
                }
                metaData.setName(name);
                // 设计一个topic对应多个监听器，用于处理不同的业务
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
