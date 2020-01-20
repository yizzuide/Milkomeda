package com.github.yizzuide.milkomeda.fusion;

import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.*;

/**
 * Fusion
 *
 * @author yizzuide
 * @since 1.12.0
 * @version 2.4.0
 * Create at 2019/08/09 10:28
 */
@Documented
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface Fusion {
    /**
     * 打个标签，用于在转换器<code>converter</code>里识别
     * @return String
     */
    @AliasFor("value")
    String tag() default "fusion";

    /**
     * 同tag
     * @return String
     */
    @AliasFor("tag")
    String value() default "fusion";

    /**
     * 根据条件是否修改返回值，需使用SpEL（默认执行修改操作，如果条件不成功，则不修改）
     * @return  String
     */
    String condition() default "";

    /**
     * 是否允许执行业务方法，需使用SpEL（设置了该属性则不再执行修改返回值操作，如果条件不成立，则不执行方法体）
     * @return String
     */
    String allowed() default "";

    /**
     * 当`allowed`方法属性为false时，可选设置反馈的方法，需使用SpEL
     * @return String
     */
    String fallback() default "";
}
