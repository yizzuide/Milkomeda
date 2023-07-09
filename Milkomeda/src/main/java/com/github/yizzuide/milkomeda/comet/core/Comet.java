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

package com.github.yizzuide.milkomeda.comet.core;

import com.github.yizzuide.milkomeda.comet.collector.CometCollectorProperties;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Provide request log, collect log which must config `comet.collector.enable` of {@link CometCollectorProperties}.
 *
 * @author yizzuide
 * @since 0.2.0
 * @version 3.15.0
 * <br>
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
     * 请求标识码
     * @return String
     */
    String apiCode() default "";

    /**
     * 请求类型（1: 前台请求（默认） 2：第三方服务器推送）
     * @return String
     */
    String requestType() default "1";

    /**
     * 设置记录数据 prototype（原型）的相应tag，用于分类
     * @return String
     */
    String tag() default "";

    /**
     * 设置记录数据 prototype（原型）如下：
     * <pre>
     * 1. CometData类型应该是一个 pojo，需要提供无参构造器
     * 2. 原则上，一个记录数据原型对应指定一个`tag`
     * </pre>
     * @return WebCometData子类型
     */
    Class<? extends WebCometData> prototype() default WebCometData.class;
}
