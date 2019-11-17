package com.github.yizzuide.milkomeda.ice;

import java.lang.annotation.*;

/**
 * IceListener
 * Topic监听器注解
 *
 * @author yizzuide
 * @since 1.15.0
 * Create at 2019/11/17 17:29
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
public @interface IceListener {
    /**
     * 监听的Topic
     */
    String value() default "";
    /**
     * 监听的Topic
     */
    String topic() default "";
}
