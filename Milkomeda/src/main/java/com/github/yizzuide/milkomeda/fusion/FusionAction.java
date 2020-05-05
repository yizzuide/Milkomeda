package com.github.yizzuide.milkomeda.fusion;

import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.*;

/**
 * FusionAction
 *
 * @author yizzuide
 * @since 3.3.0
 * Create at 2020/05/05 16:25
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
public @interface FusionAction {
    /**
     * 监听的tag
     * @return tag name
     */
    @AliasFor("tag")
    String value() default "";

    /**
     * 监监听的tag
     * @return tag name
     */
    @AliasFor("value")
    String tag() default "";
}
