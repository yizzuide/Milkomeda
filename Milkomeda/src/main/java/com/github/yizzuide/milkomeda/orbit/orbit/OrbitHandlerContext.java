/*
 * Copyright (c) 2025 yizzuide All rights Reserved.
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

package com.github.yizzuide.milkomeda.orbit.orbit;

import com.github.yizzuide.milkomeda.light.LightContext;
import com.github.yizzuide.milkomeda.light.Spot;
import com.github.yizzuide.milkomeda.orbit.OrbitInvocation;
import org.springframework.lang.NonNull;

import java.io.Serializable;

/**
 * The context used by orbit handler which annotated {@link OrbitHandler} to get {@link OrbitInvocation}.
 *
 * @since 3.21.0
 * @author yizzuide
 * Create at 2025/05/18 18:17
 */
public class OrbitHandlerContext {

    private static final LightContext<Serializable, OrbitInvocation> lightContext = new LightContext<>();

    public static void setInvocation(OrbitInvocation invocation) {
        lightContext.setData(invocation);
    }

    @NonNull
    public static OrbitInvocation getInvocation() {
        Spot<Serializable, OrbitInvocation> invocationSpot = lightContext.get();
        return invocationSpot.getData();
    }

    public static void clear() {
        lightContext.remove();
    }

}
