package com.github.yizzuide.milkomeda.comet;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Comet
 * 请求方法采集器
 *
 * @author yizzuide
 * @since 0.2.0
 * @version 1.12.0
 * Create at 2019/04/11 19:25
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface Comet {
    /**
     * 日志记录名
     * @return String
     */
    String name() default "";

    /**
     * 日志描述
     * @deprecated deprecated at 1.12.0，use <code>name</code>.
     * @return String
     */
    String description() default "";

    /**
     * 请求编码
     * @return String
     */
    String apiCode() default "";

    /**
     * 请求类型  1: 前台请求（默认） 2：第三方服务器推送
     * @return String
     */
    String requestType() default "1";

    /**
     * 设置记录数据 prototype（原型）的相应tag，用于分类
     * @return String
     */
    String tag() default "";

    /**
     * 设置记录数据 prototype（原型）
     * 注意：
     * 1. CometData类型应该是一个 pojo，需要提供无参构造器
     * 2. 原则上一个记录数据原型对应指定一个 tag
     *
     * @return WebCometData子类型
     */
    Class<? extends WebCometData> prototype() default WebCometData.class;
}
