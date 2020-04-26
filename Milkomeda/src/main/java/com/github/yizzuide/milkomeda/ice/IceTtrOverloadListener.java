package com.github.yizzuide.milkomeda.ice;

import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.*;

/**
 * IceTtrOverloadListener
 * TTR重试超载监听器
 *
 * @author yizzuide
 * @since 3.0.0
 * Create at 2020/04/04 11:42
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
public @interface IceTtrOverloadListener {
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
