/*
 * Copyright (c) 2022 yizzuide All rights Reserved.
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

// 顶级属性和方法指定生成Java调用类名
@file:JvmName("Strings")

package com.github.yizzuide.milkomeda.util

import com.github.yizzuide.milkomeda.universe.polyfill.SpringPolyfill

/**
 * StringExtent
 * 字符串扩展
 *
 * @author yizzuide
 * <br>
 * Create at 2022/08/06 17:55
 * @since 3.13.0
 */
class StringExtensionsKt {
}

/**
 * 判断字符串是否为空
 * @since 3.13.0
 */
fun isEmpty(str : String?) = SpringPolyfill.isEmpty(str)

/**
 * Object to String，return self if null.
 * @param obj object value
 * @return string value
 * @since 3.13.0
 */
fun toNullableString(obj : Any?) : String? = obj?.toString()