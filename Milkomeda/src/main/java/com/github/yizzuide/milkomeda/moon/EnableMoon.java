package com.github.yizzuide.milkomeda.moon;

import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

/**
 * EnableMoon
 *
 * @author yizzuide
 * @since 3.0.0
 * Create at 2020/03/28 17:40
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
@Import(MoonConfig.class)
public @interface EnableMoon {
}
