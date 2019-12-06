package com.github.yizzuide.milkomeda.light;

import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

/**
 * EnableLight
 *
 * @author yizzuide
 * @version 1.17.0
 * Create at 2019/12/03 16:21
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
@Inherited
@Import(LightConfig.class)
public @interface EnableLight {
}
