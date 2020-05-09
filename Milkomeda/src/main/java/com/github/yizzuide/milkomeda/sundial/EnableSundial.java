package com.github.yizzuide.milkomeda.sundial;

import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

/**
 * Enable 模块
 * @date 2020/5/8
 * @author jsq 786063250@qq.com
 * @since 3.4.0
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
@Inherited
@Import(SundialConfig.class)
public @interface EnableSundial {
}
