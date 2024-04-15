/*
 * Copyright (c) 2022 yizzuide All rights Reserved.
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

import com.github.yizzuide.milkomeda.universe.engine.el.ELContext;
import com.github.yizzuide.milkomeda.universe.engine.el.EvaluateSource;
import com.github.yizzuide.milkomeda.universe.function.TripleFunction;
import com.github.yizzuide.milkomeda.universe.metadata.HandlerMetaData;
import org.springframework.aop.framework.autoproxy.AutoProxyUtils;
import org.springframework.aop.scope.ScopedProxyUtils;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ClassPathBeanDefinitionScanner;
import org.springframework.core.MethodIntrospector;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.lang.Nullable;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ReflectionUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Spring Bean class build, register, scan, and find annotation method.
 *
 * @author yizzuide
 * @since 3.13.0
 * <br>
 * Create at 2022/09/04 18:17
 */
public final class SpringContext {

    /**
     * 构建BeanDefinition
     * @param clazz bean类
     * @param kvMap 属性键值Map
     * @param args  构造参数
     * @return  BeanDefinition
     */
    public static BeanDefinition build(Class<?> clazz, Map<String, Object> kvMap, Object... args) {
        BeanDefinitionBuilder beanDefinitionBuilder = BeanDefinitionBuilder.genericBeanDefinition(clazz);
        for (Object arg : args) {
            beanDefinitionBuilder.addConstructorArgValue(arg);
        }
        if (!CollectionUtils.isEmpty(kvMap)) {
            kvMap.forEach(beanDefinitionBuilder::addPropertyValue);
        }
        return beanDefinitionBuilder.getRawBeanDefinition();
    }

    /**
     *  动态注册并返回bean
     * @param applicationContext    应用上下文
     * @param name                  bean name
     * @param clazz                 bean class
     * @param args                  构造参数
     * @param <T>                   实体类型
     * @return  Bean
     */
    public static <T> T registerBean(ConfigurableApplicationContext applicationContext, String name, Class<T> clazz, Object... args) {
        return registerBean(applicationContext, name, clazz, null, args);
    }

    /**
     *  动态注册并返回bean
     * @param applicationContext    应用上下文
     * @param name                  bean name
     * @param clazz                 bean class
     * @param kvMap                 属性键值Map
     * @param args                  构造参数
     * @param <T>                   实体类型
     * @return  Bean
     * @since 1.13.0
     */
    public static <T> T registerBean(ConfigurableApplicationContext applicationContext, String name, Class<T> clazz, Map<String, Object> kvMap, Object... args) {
        if (applicationContext.containsBean(name)) {
            return applicationContext.getBean(name, clazz);
        }
        BeanDefinition beanDefinition = build(clazz, kvMap, args);
        return registerBean(applicationContext, name, clazz, beanDefinition);
    }

    /**
     * 注册BeanDefinition
     * @param applicationContext    应用上下文
     * @param name                  bean name
     * @param clazz                 bean class
     * @param beanDefinition        BeanDefinition
     * @param <T>                   实体类型
     * @return  Bean
     */
    public static <T> T registerBean(ConfigurableApplicationContext applicationContext, String name, Class<T> clazz, BeanDefinition beanDefinition) {
        BeanDefinitionRegistry beanFactory = (BeanDefinitionRegistry) applicationContext.getBeanFactory();
        beanFactory.registerBeanDefinition(name, beanDefinition);
        return applicationContext.getBean(name, clazz);
    }

    /**
     * 根据类上的注解扫描bean
     * @param registry                  BeanDefinitionRegistry
     * @param presentAnnotationClass    类上的注解
     * @param basePackages              扫描的包
     * @param <T>                       返回bean的类型
     * @return  bean集合
     * @since 3.13.0
     */
    @SuppressWarnings("unchecked")
    public static <T> Collection<T> scanBeans(BeanDefinitionRegistry registry, Class<? extends Annotation> presentAnnotationClass, String... basePackages) {
        ClassPathBeanDefinitionScanner scanner = new ClassPathBeanDefinitionScanner(registry);
        scanner.addIncludeFilter(new AnnotationTypeFilter(presentAnnotationClass));
        int count = scanner.scan(basePackages);
        if (count == 0) {
            return Collections.emptyList();
        }
        return ((ConfigurableListableBeanFactory) registry).getBeansWithAnnotation(presentAnnotationClass)
                .values().stream().map(o -> (T) o).collect(Collectors.toList());
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

    /**
     * Get the method map with annotation from target Class
     * @param annotationClass   annotation type
     * @param targetType        target class
     * @param <A>               annotation generic type
     * @return                  method map with annotation
     * @since 3.13.0
     */
    public static <A extends Annotation>  Map<Method, A> getTypeHandlerMap(Class<A> annotationClass, final Class<?> targetType) {
        return MethodIntrospector.selectMethods(targetType,
                (MethodIntrospector.MetadataLookup<A>) method ->
                        AnnotatedElementUtils.findMergedAnnotation(method, annotationClass));
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
                if (ELContext.match(name)) {
                    name = ELContext.getValue(EvaluateSource.builder().target(target)
                            .targetClass(target.getClass()).method(method).args(new Object[]{}).build(), name, String.class);
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
     * Get bean raw class from beanName
     * @param beanFactory   ConfigurableListableBeanFactory
     * @param beanName      bean name
     * @return  Class
     * @since 3.13.0
     */
    public static Class<?> getBeanType(ConfigurableListableBeanFactory beanFactory, @Nullable String beanName) {
        // Is target bean within a scoped proxy?
        boolean scoped = ScopedProxyUtils.isScopedTarget(beanName);
        return AutoProxyUtils.determineTargetClass(
                beanFactory, scoped ? ScopedProxyUtils.getTargetBeanName(beanName) : beanName);
    }

    /**
     * Whether the members of the target class contain annotation
     * @param annotationClass   annotation class
     * @param targetType    target class
     * @return  false if the class is known to have no such annotations at any level; true otherwise.
     * @since 3.13.0
     */
    public static boolean hasAnnotationContains(Class<Annotation> annotationClass, final Class<?> targetType) {
        return AnnotationUtils.isCandidateClass(targetType, annotationClass);
    }
}
