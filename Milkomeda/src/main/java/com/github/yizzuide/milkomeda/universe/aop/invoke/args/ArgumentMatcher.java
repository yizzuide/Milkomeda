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
 * Argument matcher interface for indicate what matcher support and calculate matched index.
 *
 * @since 3.15.0
 * @author yizzuide
 * <br>
 * Create at 2023/01/11 19:24
 */
public interface ArgumentMatcher {

    /**
     * Indicate what matcher support.
     * @param argumentDefinition ArgumentDefinition
     * @return true if matched success
     */
    boolean support(ArgumentDefinition argumentDefinition);

    /**
     * Find the index position of parameters according to method and argument definition.
     * @param method    invoked method
     * @param argumentDefinition   ArgumentDefinition
     * @return -1 if not find
     */
    int matchIndex(Method method, ArgumentDefinition argumentDefinition);

    /**
     * Find the index position of parameters and add it to args array.
     * @param args  argument array
     * @param method    invoked method
     * @param argumentDefinition ArgumentDefinition
     */
    void matchToAdd(Object[] args, Method method, ArgumentDefinition argumentDefinition);
}
