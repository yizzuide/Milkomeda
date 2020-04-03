package com.github.yizzuide.milkomeda.universe.context;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * WebContext
 *
 * @author yizzuide
 * @since 1.14.0
 * @version 3.0.0
 * Create at 2019/11/11 21:38
 */
public class WebContext {
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
        if (applicationContext.containsBean(name)) {
            return applicationContext.getBean(name, clazz);
        }
        BeanDefinition beanDefinition = build(clazz, args);
        return registerBean(applicationContext, name, clazz, beanDefinition);
    }

    /**
     * 构建BeanDefinition
     * @param clazz bean类
     * @param args  构造参数
     * @return  BeanDefinition
     */
    public static BeanDefinition build(Class<?> clazz, Object... args) {
        BeanDefinitionBuilder beanDefinitionBuilder = BeanDefinitionBuilder.genericBeanDefinition(clazz);
        for (Object arg : args) {
            beanDefinitionBuilder.addConstructorArgValue(arg);
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
}
