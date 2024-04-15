/*
 * Copyright (c) 2024 yizzuide All rights Reserved.
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

package com.github.yizzuide.milkomeda.comet.core;

import com.github.yizzuide.milkomeda.light.LightContext;

import java.util.HashMap;
import java.util.Stack;

/**
 * The Context of XComet which provide stack structure data when invoke within nest method.
 *
 * @since 4.0.0
 * @author yizzuide
 * <br>
 * Create at 2024/01/29 21:41
 */
public class XCometContext {

    private static final LightContext<?, Stack<XCometData>> lightContext = new LightContext<>();

    static void init() {
        lightContext.setData(new Stack<>());
    }

    public static void push(XCometData cometData) {
        stateCheck();
        lightContext.get().getData().push(cometData);
    }

    public static XCometData pop() {
        stateCheck();
        return lightContext.get().getData().pop();
    }

    public static XCometData peek() {
        if (lightContext.get() == null) {
            return null;
        }
        Stack<XCometData> cometDataStack = lightContext.get().getData();
        if (cometDataStack.isEmpty()) {
            return null;
        }
        return cometDataStack.peek();
    }

    public static void putVariable(String name, Object value) {
        stateCheck();
        XCometData cometData = lightContext.get().getData().peek();
        if (cometData != null) {
            if (cometData.getVariables() == null) {
                cometData.setVariables(new HashMap<>());
            }
            cometData.getVariables().put(name, value);
        }
    }

    static void clear() {
        lightContext.remove();
    }

    private static void stateCheck() {
        if (lightContext.get() == null) {
            throw new IllegalStateException("Context data is null, please add `@EnableComet` with application config first.");
        }
    }
}
