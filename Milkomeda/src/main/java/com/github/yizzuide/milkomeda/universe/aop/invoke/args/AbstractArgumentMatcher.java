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

import org.springframework.aop.support.AopUtils;
import org.springframework.core.LocalVariableTableParameterNameDiscoverer;

import java.lang.reflect.Method;

/**
 * An abstract argument matcher that has ability to discover param name.
 *
 * @since 3.15.0
 * @author yizzuide
 * <br>
 * Create at 2023/01/12 02:21
 */
public abstract class AbstractArgumentMatcher implements ArgumentMatcher {
    private final LocalVariableTableParameterNameDiscoverer discoverer = new LocalVariableTableParameterNameDiscoverer();

    @Override
    public int matchIndex(Method method, ArgumentDefinition argumentDefinition) {
        // get target method which warped proxy
        method = AopUtils.getMostSpecificMethod(method, method.getDeclaringClass());
        String[] parameterNames = discoverer.getParameterNames(method);
        Class<?>[] parameterTypes = method.getParameterTypes();
        if (parameterNames == null || parameterNames.length == 0) {
            return -1;
        }
        return doMatchIndex(parameterNames, parameterTypes, argumentDefinition);
    }

    protected abstract int doMatchIndex(String[] parameterNames, Class<?>[] parameterTypes, ArgumentDefinition argumentDefinition);

    @Override
    public void matchToAdd(Object[] args, Method method, ArgumentDefinition argumentDefinition) {
        int index = matchIndex(method, argumentDefinition);
        if (index > -1) {
            args[index] = argumentDefinition.getValue();
        }
    }
}
