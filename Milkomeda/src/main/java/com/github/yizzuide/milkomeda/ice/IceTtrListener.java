package com.github.yizzuide.milkomeda.ice;

import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.*;

/**
 * IceTtrListener
 * TTR重试监听器，方法参数类型固定为 {@link TtrJob}
 *
 * @author yizzuide
 * @since 3.2.0
 * Create at 2020/04/26 10:37
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
public @interface IceTtrListener {
    /**
     * 监听的Topic
     *
     * @return topic name
     */
    @AliasFor("topic")
    String value() default "";

    /**
     * 监听的Topic
     *
     * @return topic name
     */
    @AliasFor("value")
    String topic() default "";
}
