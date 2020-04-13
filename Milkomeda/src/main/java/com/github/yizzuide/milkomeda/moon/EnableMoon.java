package com.github.yizzuide.milkomeda.moon;

import com.github.yizzuide.milkomeda.light.EnableLight;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

/**
 * EnableMoon
 *
 * @author yizzuide
 * @since 3.0.0
 * @version 3.0.2
 * Create at 2020/03/28 17:40
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
@EnableLight
@Import(MoonConfig.class)
public @interface EnableMoon {
}
