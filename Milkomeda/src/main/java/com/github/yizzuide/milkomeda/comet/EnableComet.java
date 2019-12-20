package com.github.yizzuide.milkomeda.comet;

import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

/**
 * EnableComet
 *
 * @author yizzuide
 * @since 2.0.0
 * Create at 2019/12/13 00:56
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
@Import(CometConfig.class)
public @interface EnableComet {
}
