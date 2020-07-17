package com.github.yizzuide.milkomeda.halo;

import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

/**
 * EnableHalo
 *
 * @author yizzuide
 * @since 2.5.0
 * @version 3.11.4
 * Create at 2020/01/30 18:42
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
@Import(HaloConfig.class)
public @interface EnableHalo {
}
