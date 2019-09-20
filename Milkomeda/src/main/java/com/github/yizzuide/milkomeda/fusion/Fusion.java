package com.github.yizzuide.milkomeda.fusion;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Fusion
 *
 * @author yizzuide
 * @since 1.12.0
 * Create at 2019/08/09 10:28
 */
@Target({ElementType.PARAMETER, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface Fusion {
    /**
     * 类型标签
     */
    String tag() default "fusion";

    /**
     * 同tag
     */
    String value() default "";
}
