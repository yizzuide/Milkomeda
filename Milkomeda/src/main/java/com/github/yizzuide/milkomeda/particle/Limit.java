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

package com.github.yizzuide.milkomeda.particle;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Limit
 * 基于Redis的粒子限制注解
 *
 * 支持使用拦截链限制器，如：<code>BarrierLimiter</code>
 * 注意：注解方式和调用内置方法方式不能一起使用！
 *
 * @author yizzuide
 * @since 1.5.0
 * @version 3.12.10
 * <br>
 * Create at 2019/05/30 22:22
 */
@Target({ElementType.PARAMETER, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface Limit {
    /**
     * 前缀标识名
     * @return String
     */
    String name() default "";

    /**
     * 唯一标识键，用于组成redis的key，支持SpEl的多种获取方式，如：
     * <ul>
     *     <li> 参数采集: #id </li>
     *     <li> 请求域: #request.getHeader('token') </li>
     *     <li> 自定义配置: #env['key'] </li>
     *     <li> Metal动态配置（依赖Metal模块的配置加载）：#metal['key']</li>
     *     <li> Spring配置: @env.get('spring.application.name') </li>
     * </ul>
     * @return String
     */
    String key() default "";

    /**
     * 限制器组件名，优先级比limiterBeanClass高
     * @return String
     */
    String limiterBeanName() default "";

    /**
     * 限制器类型，如果设置了limiterBeanName则不生效，默认为IdempotentLimiter
     * @return Class
     */
    Class<? extends Limiter> limiterBeanClass() default IdempotentLimiter.class;
}
