/*
 * Copyright (c) 2021 yizzuide All rights Reserved.
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.github.yizzuide.milkomeda.fusion;

import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.*;

/**
 * Fusion
 *
 * @author yizzuide
 * @since 1.12.0
 * @version 3.13.0
 * Create at 2019/08/09 10:28
 */
@Documented
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Repeatable(Fusions.class)
public @interface Fusion {
    /**
     * 打个标签，用于在转换器<code>converter</code>里识别
     * @see #condition()
     * @return String
     */
    @AliasFor("value")
    String tag() default "";

    /**
     * 同tag
     * @return String
     */
    @AliasFor("tag")
    String value() default "";

    /**
     * 根据条件是否修改返回值，`allowed`为false时设置有效，需使用SpEL（默认执行修改操作，如果条件不成功，则不修改）
     * @see #tag()
     * @return  String
     */
    String condition() default "";

    /**
     * 是否允许执行业务方法，需使用SpEL（设置了该属性则不再执行修改返回值操作，如果条件不成立，则不执行方法体）
     * @see #allowedType()
     * @see #fallback()
     * @return String
     */
    String allowed() default "";

    /**
     * 允许逻辑执行类型，需要配合注解<code>@FusionGroup</code>使用
     * @see #allowed()
     * @return  FusionAllowedType
     */
    FusionAllowedType allowedType() default FusionAllowedType.AND;

    /**
     * 当`allowed`方法属性为false时，可选设置反馈的方法，需使用SpEL
     * @see #allowed()
     * @return String
     */
    String fallback() default "";
}
