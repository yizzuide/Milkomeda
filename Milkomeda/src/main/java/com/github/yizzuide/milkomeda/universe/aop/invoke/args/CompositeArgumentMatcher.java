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
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

/**
 * Collecting argument matcher and handle batch.
 *
 * @since 3.15.0
 * @author yizzuide
 * <br>
 * Create at 2023/01/11 19:25
 */
public class CompositeArgumentMatcher implements ArgumentMatcher {

    private final Set<ArgumentMatcher> argumentMatchers = new HashSet<>();

    public void addMatcher(ArgumentMatcher matcher) {
        argumentMatchers.add(matcher);
    }

    @Override
    public boolean support(ArgumentDefinition argumentDefinition) {
        return argumentMatchers.stream().anyMatch(a -> a.support(argumentDefinition));
    }

    @Override
    public int matchIndex(Method method, ArgumentDefinition argumentDefinition) {
        Optional<ArgumentMatcher> selectedArgumentMatcher = argumentMatchers.stream().filter(a -> a.support(argumentDefinition)).findFirst();
        return selectedArgumentMatcher.map(matcher -> matcher.matchIndex(method, argumentDefinition)).orElse(-1);
    }

    @Override
    public void matchToAdd(Object[] args, Method method, ArgumentDefinition argumentDefinition) {
        argumentMatchers.stream().filter(a -> a.support(argumentDefinition))
                .findFirst().ifPresent(a -> a.matchToAdd(args, method, argumentDefinition));
    }
}
