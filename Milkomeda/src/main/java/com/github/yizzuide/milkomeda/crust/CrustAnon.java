package com.github.yizzuide.milkomeda.crust;

import java.lang.annotation.*;

/**
 * CrustAnon
 * 标记匿名访问URL
 *
 * @author yizzuide
 * @since 3.5.0
 * Create at 2020/05/13 11:31
 */
@Documented
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface CrustAnon {
}
