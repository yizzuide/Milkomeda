package com.github.yizzuide.milkomeda.fusion;

import java.lang.annotation.*;

/**
 * FusionGroup
 *
 * @author yizzuide
 * @since 3.0.0
 * Create at 2020/04/01 00:51
 */
@Documented
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface FusionGroup {
    Fusion[] value();
}
