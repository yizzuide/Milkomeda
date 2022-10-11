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

package com.github.yizzuide.milkomeda.echo;

/**
 * ResponseData
 * 响应类规范接口，请求时返回类型统一使用这个接口
 * @param <T> data类型
 *
 * @author yizzuide
 * @since 1.13.0
 * @version 1.13.3
 * <br>
 * Create at 2019/09/21 17:13
 */
public interface EchoResponseData<T> {
    /**
     * 响应码
     * @return String
     */
    String getCode();
    default void setCode(String code) {}

    /**
     * 响应消息
     * @return String
     */
    String getMsg();
    default void setMsg(String msg) {}

    /**
     * 响应业务数据
     * @return T
     */
    T getData();
    default void setData(T respData) {}
}
