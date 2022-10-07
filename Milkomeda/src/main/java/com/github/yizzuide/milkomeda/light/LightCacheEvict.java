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

package com.github.yizzuide.milkomeda.light;

import java.lang.annotation.*;

/**
 * LightCacheEvict
 *
 * @author yizzuide
 * @since 2.0.0
 * @version 2.0.3
 * <br />
 * Create at 2019/12/18 14:31
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
@Inherited
public @interface LightCacheEvict {
    /**
     * 缓存实例名（不同的缓存类型应该设置不能的名字），支持EL表达式
     * @return  String
     */
    String value();

    /**
     * 缓存key，支持EL表达式
     * @return  String
     */
    String key();

    /**
     * 缓存key前辍，与属性方法 key() 合成完整的key
     * @return String
     */
    String keyPrefix() default "";

    /**
     * 缓存条件，需要使用EL表达式
     * @return String
     */
    String condition() default "";
}
