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

import java.text.MessageFormat;

/**
 * UniformExceptionAssert
 * 统一异步断言，需要被枚举类实现
 *
 * @author yizzuide
 * @since 3.10.0
 * Create at 2020/07/03 17:10
 */
public interface UniformExceptionDataAssert extends UniformExceptionData, UniformExceptionAssert {

    @Override
    default UniformException newException(Object... args) {
        String msg = this.getMessage();
        if (args != null) {
            msg = MessageFormat.format(this.getMessage(), args);
        }
        return new UniformException(this, msg);
    }

    @Override
    default UniformException newException(Throwable t, Object... args) {
        String msg = this.getMessage();
        if (args != null) {
            msg = MessageFormat.format(this.getMessage(), args);
        }
        return new UniformException(this, msg, t);
    }
}
