package com.github.yizzuide.milkomeda.particle;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Limit
 * 基于Redis的粒子限制注解
 *
 * 支持使用拦截链限制器，如：<code>BarrierLimiter</code>
 * 注意：注解方式和调用内置方法方式不能一起使用！
 *
 * @author yizzuide
 * @since 1.5.0
 * @version 3.1.0
 * Create at 2019/05/30 22:22
 */
@Target({ElementType.PARAMETER, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface Limit {
    /**
     * 前缀标识名
     * @return String
     */
    String name() default "";

    /**
     * 唯一标识键，用于组成redis的key，支持Spring EL表达式，如：#id、#request.getHeader('token')、#env['title']、@env.get('spring.application.name')
     * @return String
     */
    String key() default "";

    /**
     * 过期时间。单位：秒
     * @return long
     */
    long expire() default -1L;

    /**
     * 限制器组件名，优先级比limiterBeanClass高
     * @return String
     */
    String limiterBeanName() default "";

    /**
     * 限制器类型，如果设置了limiterBeanName则不生效，默认为IdempotentLimiter
     * @return Class
     */
    Class<? extends Limiter> limiterBeanClass() default IdempotentLimiter.class;
}
