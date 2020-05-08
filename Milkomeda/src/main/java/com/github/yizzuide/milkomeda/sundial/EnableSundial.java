package com.github.yizzuide.milkomeda.sundial;

import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

/**
 * @date: 2020/5/8
 * @author: jsq
 * @email: 786063250@qq.com
 * @describe:
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
@Inherited
@Import(SundialConfig.class)
public @interface EnableSundial {
}
