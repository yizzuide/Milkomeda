package com.github.yizzuide.milkomeda.comet;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Comet
 * 采集注解
 *
 * @author yizzuide
 * @since 0.2.0
 * Create at 2019/04/11 19:25
 */
@Target({ElementType.PARAMETER, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface Comet {
    /**
     * 请求编码
     * @return String
     */
    String apiCode() default "";

    /**
     * 接口描述
     * @return String
     */
    String description() default "";

    /**
     *  请求类型  1: 前台请求（默认） 2：第三方服务器推送
     * @return String
     */
    String requestType() default "1";
}
