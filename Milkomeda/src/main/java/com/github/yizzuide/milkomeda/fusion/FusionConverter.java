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

/**
 * FusionConverter
 *
 * @author yizzuide
 * @since 2.2.0
 * @version 3.0.0
 * Create at 2020/01/04 11:14
 */
@FunctionalInterface
public interface FusionConverter<T, O, R> {
    /**
     * 错误消息前缀
     */
    String ERROR_PREFIX = "#E.";

    /**
     * 构建错误消息
     * @param msg   原消息
     * @return  错误消息
     */
    static String buildError(String msg) {
        return ERROR_PREFIX + msg;
    }

    /**
     * 修改返回值
     * @param tag           转换类型tag
     * @param returnObj     返回的原方法数据
     * @param error         业务错误
     * @return  替换后的返回数据
     */
    R apply(T tag, O returnObj, String error);
}
