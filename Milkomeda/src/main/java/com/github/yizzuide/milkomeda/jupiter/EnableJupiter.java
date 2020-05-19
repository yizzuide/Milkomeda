package com.github.yizzuide.milkomeda.jupiter;

import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

/**
 * EnableJupiter
 *
 * @author yizzuide
 * Create at 2020/05/19 16:57
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
@Inherited
@Import(JupiterConfig.class)
public @interface EnableJupiter {
}
