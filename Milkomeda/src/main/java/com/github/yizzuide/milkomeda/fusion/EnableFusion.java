package com.github.yizzuide.milkomeda.fusion;

import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

/**
 * EnableFusion
 *
 * @author yizzuide
 * @version 2.0.0
 * Create at 2019/12/13 00:54
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
@Import(FusionConfig.class)
public @interface EnableFusion {
}
