package com.github.yizzuide.milkomeda.hydrogen.core;

import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

/**
 * EnableHydrogen
 *
 * @author yizzuide
 * @since 2.8.0
 * Create at 2020/03/25 17:46
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
@Import(HydrogenConfig.class)
public @interface EnableHydrogen {
}
