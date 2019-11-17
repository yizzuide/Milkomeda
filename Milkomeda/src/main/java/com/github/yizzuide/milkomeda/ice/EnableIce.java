package com.github.yizzuide.milkomeda.ice;

import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

/**
 * EnableIce
 *
 * @author yizzuide
 * @since 1.15.0
 * Create at 2019/11/16 17:24
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
@Inherited
@Import({IceConfig.class, IceScheduleConfig.class})
public @interface EnableIce {
}
