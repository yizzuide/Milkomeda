package com.github.yizzuide.milkomeda.metal;

import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.*;

/**
 * Metal
 *
 * @author yizzuide
 * @since 3.6.0
 * Create at 2020/05/21 18:22
 */
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
public @interface Metal {
    /**
     * 配置名
     * @return String
     */
    @AliasFor("name")
    String value() default "";

    /**
     * 配置名
     * @return String
     */
    @AliasFor("value")
    String name() default "";
}
