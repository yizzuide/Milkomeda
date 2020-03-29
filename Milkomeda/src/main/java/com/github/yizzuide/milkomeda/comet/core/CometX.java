package com.github.yizzuide.milkomeda.comet.core;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * CometX
 * 业务方法采集器
 *
 * @author yizzuide
 * @since 1.12.0
 * Create at 2019/09/21 01:15
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface CometX {
    /**
     * 日志记录名
     * @return String
     */
    String name() default "";

    /**
     * 设置记录数据 prototype（原型）的相应tag，用于分类
     * 不同的 tag 对应不同的记录数据原型
     * @return String
     */
    String tag() default "";

    /**
     * 设置记录数据 prototype（原型）
     * 注意：
     * 1. CometData类型应该是一个 pojo，需要提供无参构造器
     * 2. 原则上一个记录数据原型对应指定一个 tag
     *
     * @return XCometData子类型
     */
    Class<? extends XCometData> prototype() default XCometData.class;

}
