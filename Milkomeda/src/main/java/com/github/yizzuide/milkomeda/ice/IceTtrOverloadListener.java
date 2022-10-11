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

package com.github.yizzuide.milkomeda.ice;

import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.*;

/**
 * IceTtrOverloadListener
 * TTR重试超载监听器
 *
 * <pre>
 *     通过配置 `ice.enable-job-timer-distributed` 配合Dead queue的使用有以下情况：
 *     1. TTR重试超载监听器未添加或执行失败 + 未开启Dead queue：通过计算延迟因子继续重试，并有错误级别的日志打印
 *     2. TTR重试超载监听器未添加或执行失败 + 开启Dead queue：延迟任务进入Dead queue，原数据保留
 *     3. TTR重试超载监听器执行成功 + 未开启Dead queue：延迟任务和原数据都将被移除
 *     4. TTR重试超载监听器执行成功 + 开启Dead queue：延迟任务进入Dead queue，原数据保留
 * </pre>
 *
 * @author yizzuide
 * @since 3.0.0
 * <br>
 * Create at 2020/04/04 11:42
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
public @interface IceTtrOverloadListener {
    /**
     * 监听的Topic
     *
     * @return topic name
     */
    @AliasFor("topic")
    String value() default "";

    /**
     * 监听的Topic
     *
     * @return topic name
     */
    @AliasFor("value")
    String topic() default "";
}
