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
     *  动态注册bean
     * @param applicationContext    上下文
     * @param name                  bean name
     * @param clazz                 bean class
     * @param args                  构造参数
     * @param <T>                   实体类型
     * @return  Bean
     */
    public static <T> T registerBean(ConfigurableApplicationContext applicationContext, String name, Class<T> clazz, Object... args) {
        if (applicationContext.containsBean(name)) {
            T bean = applicationContext.getBean(name, clazz);
            if (bean.getClass().isAssignableFrom(clazz)) {
                return bean;
            } else {
                throw new RuntimeException("BeanName 重复 " + name);
            }
        }
        BeanDefinitionBuilder beanDefinitionBuilder = BeanDefinitionBuilder.genericBeanDefinition(clazz);
        for (Object arg : args) {
            beanDefinitionBuilder.addConstructorArgValue(arg);
        }
        BeanDefinition beanDefinition = beanDefinitionBuilder.getRawBeanDefinition();

        BeanDefinitionRegistry beanFactory = (BeanDefinitionRegistry) applicationContext.getBeanFactory();
        beanFactory.registerBeanDefinition(name, beanDefinition);
        return applicationContext.getBean(name, clazz);
    }
}
