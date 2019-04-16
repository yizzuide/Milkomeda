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
 * @version 0.2.4
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

    /**
     * 设置记录数据 prototype（原型）的相应tag，用于请求分类，用于收集不同的类型数据，
     * 不同的 tag 对应不同的记录数据原型
     * @return String
     */
    String tag() default "";

    /**
     * 设置记录数据 prototype（原型）, 与tag 对应，用于收集不同的类型数据
     * 注意：
     * 1. CometData类型应该是一个 pojo，需要提供无参构造器
     * 2. 原则上一个记录数据原型对应指定一个 tag
     *
     * @return CometData子类型
     */
    Class<? extends CometData> prototype() default CometData.class;
}
