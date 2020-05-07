package com.github.yizzuide.milkomeda.fusion;

import org.springframework.stereotype.Component;

import java.lang.annotation.*;

/**
 * FusionHandler
 *
 * @author yizzuide
 * @since 3.3.0
 * Create at 2020/05/05 16:24
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
@Inherited
@Component
public @interface FusionHandler {
}
