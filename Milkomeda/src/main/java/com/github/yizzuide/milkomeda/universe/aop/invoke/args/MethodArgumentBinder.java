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
import java.util.List;

/**
 * A method based argument binder for reflection invoke which auto bind args value at corresponding index with {@link ArgumentDefinition} config.
 *
 * @see org.springframework.aop.aspectj.AbstractAspectJAdvice#calculateArgumentBindings()
 * @since 3.15.0
 * @author yizzuide
 * <br>
 * Create at 2023/01/11 19:21
 */
public class MethodArgumentBinder {

    private static volatile CompositeArgumentMatcher argumentMatchers;

    /**
     * Get bind args from argument definition list.
     * @param argumentSources   argument definition list
     * @param method    target method
     * @return args
     */
    public static Object[] bind(ArgumentSources argumentSources, Method method) {
        CompositeArgumentMatcher argumentMatchers = getArgumentMatchers();
        List<ArgumentDefinition> argumentDefinitions = argumentSources.getList();
        int size = method.getParameters().length;
        Object[] args = new Object[size];
        for (ArgumentDefinition argumentDefinition : argumentDefinitions) {
            if (argumentMatchers.support(argumentDefinition)) {
                argumentMatchers.matchToAdd(args, method, argumentDefinition);
            }
        }
        return args;
    }

    /**
     * Register argument matcher to match special argument definition.
     * @param argumentMatcher   ArgumentMatcher
     */
    public static void register(ArgumentMatcher argumentMatcher) {
        getArgumentMatchers().addMatcher(argumentMatcher);
    }

    private static CompositeArgumentMatcher getArgumentMatchers() {
        if (argumentMatchers == null) {
            synchronized (MethodArgumentBinder.class) {
                if (argumentMatchers == null) {
                    argumentMatchers = new CompositeArgumentMatcher();
                    argumentMatchers.addMatcher(new NamedTypeArgumentMatcher());
                    argumentMatchers.addMatcher(new JDKRegexArgumentMatcher());
                    argumentMatchers.addMatcher(new ResidualArgumentMatcher());
                }
            }
        }
        return argumentMatchers;
    }
}
