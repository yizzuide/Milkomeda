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

package com.github.yizzuide.milkomeda.pulsar;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * PulsarFlow
 * 该注解应用于@RequestMapping注解的方法上，能通过DeferredResult技术延迟返回结果，让数据的返回更灵活，
 * 使用场景如：长轮询、耗时请求fast-timeout等
 *
 * 注意：使用该注解的方法返回必需为 Object。（由于 SpringMVC 内部会对类型进行校验的机制，使用 Object 并不会影响实际处理结果）
 *
 * @author yizzuide
 * @since  0.1.0
 * @version 1.5.0
 * Create at 2019/03/29 10:22
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface PulsarFlow {
    /**
     * 可选标注实际返回类型，只是让代码看起来更直观
     * @return 实际返回的Class
     */
     Class<?> returnClass() default Object.class;

    /**
     * 功能和returnClass一样
     * @return 实际返回的Class
     */
    Class<?> value() default Object.class;

    /**
     * 支持使用DeferredResult方式，推迟响应返回
     * 使用方法：
     * ``` @PulsarFlow(useDeferredResult=true)
     *     public Object method(..., PulsarDeferredResult deferredResult) {
     *          ...
     *          // 设置唯一标识符
     *          deferredResult.setDeferredResultID("235495954");
     *          // 返回任意数据，但内部不会获取这个值，在其它类中通过<code>PulsarHolder.getPulsar().getDeferredResult("唯一序列号").setResult(...)</code>返回
     *          return null;
     *     }
     * ```
     *
     * @return 如果指定为 true，则使用DeferredResult，默认使用WebAsyncTask
     */
    boolean useDeferredResult() default false;

    /**
     * 设置DeferredResult的ID标识，用于在返回时查找
     * 1. 支持Spring EL表达式，如：#id
     * 2. 支持HTTP Header获取表达式（内建支持），如：@token
     * @return 如果有设置，则需要不在方法上添加PulsarDeferredResult参数来设置ID
     */
    String id() default "";
}
