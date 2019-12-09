package com.github.yizzuide.milkomeda.neutron;

import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

/**
 * EnableNeutron
 *
 * @author yizzuide
 * @since 1.18.0
 * Create at 2019/12/09 22:33
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
@Inherited
@Import(NeutronConfig.class)
public @interface EnableNeutron {
}
