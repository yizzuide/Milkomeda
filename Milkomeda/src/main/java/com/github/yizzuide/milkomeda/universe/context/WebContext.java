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

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ClassPathBeanDefinitionScanner;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.util.CollectionUtils;
import org.springframework.util.PathMatcher;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.util.UrlPathHelper;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * WebContext
 *
 * @author yizzuide
 * @since 1.14.0
 * @version 3.13.0
 * Create at 2019/11/11 21:38
 */
public class WebContext {

    /**
     * 路径匹配器
     */
    private static PathMatcher mvcPathMatcher;

    /**
     * URL路径帮助类
     */
    private static UrlPathHelper urlPathHelper;

    public static void setMvcPathMatcher(PathMatcher mvcPathMatcher) {
        WebContext.mvcPathMatcher = mvcPathMatcher;
    }

    /**
     * 路径匹配器
     * @return  PathMatcher
     */
    public static PathMatcher getMvcPathMatcher() {
        return mvcPathMatcher;
    }

    public static void setUrlPathHelper(UrlPathHelper urlPathHelper) {
        WebContext.urlPathHelper = urlPathHelper;
    }

    /**
     * 请求路径帮助类
     * @return  UrlPathHelper
     */
    public static UrlPathHelper getUrlPathHelper() {
        return urlPathHelper;
    }

    /**
     * 获取请求信息
     * @return  ServletRequestAttributes
     */
    public static ServletRequestAttributes getRequestAttributes() {
        return (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
    }

    /**
     * 获得请求对象
     * @return  HttpServletRequest
     */
    public static HttpServletRequest getRequest() {
        return getRequestAttributes().getRequest();
    }

    /**
     * 获取响应对象
     * @return HttpServletResponse
     */
    public static HttpServletResponse getResponse() {
        return getRequestAttributes().getResponse();
    }

    /**
     * 获取当前会话
     * @return  HttpSession
     */
    public static HttpSession getSession() {
        return getRequest().getSession();
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
                .values().stream().map(o -> (T)o).collect(Collectors.toList());
    }
}
