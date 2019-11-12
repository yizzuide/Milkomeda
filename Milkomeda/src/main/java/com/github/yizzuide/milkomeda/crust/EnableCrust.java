package com.github.yizzuide.milkomeda.crust;

import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

/**
 * EnableCrust
 *
 * @author yizzuide
 * @since 1.14.0
 * Create at 2019/11/11 15:14
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
@Import(CrustConfig.class)
public @interface EnableCrust {
}
