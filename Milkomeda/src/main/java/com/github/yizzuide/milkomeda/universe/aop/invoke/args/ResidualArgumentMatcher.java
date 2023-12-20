/*
 * Copyright (c) 2023 yizzuide All rights Reserved.
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

package com.github.yizzuide.milkomeda.universe.aop.invoke.args;

import java.lang.reflect.Method;

/**
 * A flag impl to tall argument binder full this last one.
 *
 * @since 3.15.0
 * @author yizzuide
 * <br>
 * Create at 2023/01/12 02:54
 */
public class ResidualArgumentMatcher implements ArgumentMatcher {

    /**
     * index is flag that indicates last one to add.
     */
   static final Integer RESIDUAL_PLACEHOLDER_INDEX = -2;

    @Override
    public boolean support(ArgumentDefinition argumentDefinition) {
        return ArgumentMatchType.Residual.equals(argumentDefinition.getMatchType());
    }

    @Override
    public int matchIndex(Method method, ArgumentDefinition argumentDefinition) {
        return RESIDUAL_PLACEHOLDER_INDEX;
    }

    @Override
    public void matchToAdd(Object[] args, Method method, ArgumentDefinition argumentDefinition) {
        for (int i = 0; i < args.length; i++) {
            if (args[i] == null) {
                args[i] = argumentDefinition.getValue();
                break;
            }
        }
    }
}
