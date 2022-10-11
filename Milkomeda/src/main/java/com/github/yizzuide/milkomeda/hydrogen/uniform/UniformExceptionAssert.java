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

package com.github.yizzuide.milkomeda.hydrogen.uniform;

/**
 * UniformAssert
 * 统一断言
 *
 * @author yizzuide
 * @since 3.10.0
 * <br>
 * Create at 2020/07/03 16:56
 */
public interface UniformExceptionAssert {

    /**
     * 创建异常
     * @param args  消息格式化参数
     * @return  UniformException
     */
    UniformException newException(Object... args);

    /**
     * 创建异常
     * @param t     被包装异常
     * @param args  消息格式化参数
     * @return  UniformException
     */
    UniformException newException(Throwable t, Object... args);

    /**
     * 断言是否为空
     * @param obj   被断言对象
     */
    default void assertNotNull(Object obj) {
        if (obj == null) {
            throw newException();
        }
    }

    /**
     * 断言是否为空
     * @param obj   被断言对象
     * @param args  消息格式化参数
     */
    default void assertNotNull(Object obj, Object... args) {
        if (obj == null) {
            throw newException(args);
        }
    }

    /**
     * 自定义条件断言
     * @param condition 断言条件
     * @param args  消息格式化参数
     */
    default void assertBool(boolean condition, Object... args) {
        if (condition) {
            throw newException(args);
        }
    }
}
