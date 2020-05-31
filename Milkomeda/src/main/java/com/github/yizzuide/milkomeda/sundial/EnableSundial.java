package com.github.yizzuide.milkomeda.sundial;

import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

/**
 * Enable 模块
 * @author jsq 786063250@qq.com
 * @since 3.4.0
 * @version 3.7.1
 * Create at 2020/5/8
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
@Inherited
@Import({SundialConfig.class, SundialTweakConfig.class})
public @interface EnableSundial {
}
