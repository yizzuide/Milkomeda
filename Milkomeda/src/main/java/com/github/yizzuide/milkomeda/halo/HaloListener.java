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
 * @version 3.11.4
 * <br />
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
    String value() default "*";

    /**
     * 监听的表名
     * @return  String
     */
    @AliasFor("value")
    String tableName() default "*";

    /**
     * 监听类型
     * @return  默认为后置监听
     */
    HaloType type() default HaloType.POST;
}
