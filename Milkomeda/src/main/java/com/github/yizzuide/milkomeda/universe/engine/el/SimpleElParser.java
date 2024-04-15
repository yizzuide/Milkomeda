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

package com.github.yizzuide.milkomeda.universe.engine.el;

/**
 * The Spring EL parser that can run independently.
 *
 * @since 3.8.0
 * @version 3.20.0
 * @author yizzuide
 * <br>
 * Create at 2020/06/16 10:25
 */
public class SimpleElParser {
    private static final ObjectExpressionEvaluator EVALUATOR = new ObjectExpressionEvaluator();

    /**
     * Parse spring EL expression and return value.
     * @param expression    EL expression
     * @param object        root object
     * @param resultType    result class type
     * @return  expression resolve result
     * @param <T>   result type
     */
    public static <T> T parse(String expression, Object object, Class<T> resultType) {
        return EVALUATOR.condition(expression, EvaluateSource.from(object), resultType);
    }
}
