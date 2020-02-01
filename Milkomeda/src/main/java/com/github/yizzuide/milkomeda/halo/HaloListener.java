package com.github.yizzuide.milkomeda.halo;

import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.*;

/**
 * HaloListener
 * 持久化事件监听器
 * <pre>
 * 注入参数如下：
 * - Object param
 *   该参数类型根据Mapper方法的参数决定，如果是一个参数，则为实体或简单数据类型；如果是多个参数，则为Map。
 * - Object result
 *   Mapper方法返回值。
 * - SqlCommandType commandType
 *   该SQL的操作类型：INSERT、UPDATE、DELETE、SELECT。
 * </pre>
 *
 * @author yizzuide
 * @since 2.5.0
 * Create at 2020/01/30 22:36
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
public @interface HaloListener {
    /**
     * 监听的表名
     * @return String
     */
    @AliasFor("tableName")
    String value() default "";

    /**
     * 监听的表名
     * @return  String
     */
    @AliasFor("value")
    String tableName() default "";

    /**
     * 监听类型
     * @return  默认为后置监听
     */
    HaloType type() default HaloType.POST;
}
