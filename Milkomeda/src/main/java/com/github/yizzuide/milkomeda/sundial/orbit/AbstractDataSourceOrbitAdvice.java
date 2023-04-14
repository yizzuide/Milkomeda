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

package com.github.yizzuide.milkomeda.sundial.orbit;


import com.github.yizzuide.milkomeda.orbit.OrbitAdvice;
import com.github.yizzuide.milkomeda.orbit.OrbitInvocation;
import com.github.yizzuide.milkomeda.sundial.SundialHolder;

/**
 * Abstract Dynamic route advice impl of {@link OrbitAdvice}.
 *
 * @since 3.4.0
 * @version 3.15.0
 * @author yizzuide
 * <br>
 * Create at 2020/05/11 16:29
 */
public abstract class AbstractDataSourceOrbitAdvice implements OrbitAdvice {

    @Override
    public Object invoke(OrbitInvocation invocation) throws Throwable {
        try {
            // 调用方法前，选择数据源
            SundialHolder.setDataSourceType(getRouteKey(invocation));
            return invocation.proceed();
        } finally {
            SundialHolder.clearDataSourceType();
        }
    }

    /**
     * The subclass should implement this method and return route key.
     * @param invocation Orbit invocation
     * @return Route key
     * @since 3.15.0
     */
    protected abstract String getRouteKey(OrbitInvocation invocation);
}
