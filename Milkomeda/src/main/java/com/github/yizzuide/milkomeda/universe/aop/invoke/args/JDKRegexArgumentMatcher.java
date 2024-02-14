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

import com.github.yizzuide.milkomeda.util.StringExtensionsKt;

import java.util.regex.Pattern;

/**
 * Regex match impl of argument matcher which using from JDK API.
 *
 * @since 3.15.0
 * @author yizzuide
 * <br>
 * Create at 2023/01/11 19:29
 */
public class JDKRegexArgumentMatcher extends AbstractArgumentMatcher {
    @Override
    public boolean support(ArgumentDefinition argumentDefinition) {
        return ArgumentMatchType.REGEX.equals(argumentDefinition.getMatchType()) &&
                (argumentDefinition.getIdentifier() != null && !StringExtensionsKt.isEmpty(argumentDefinition.getIdentifier().toString()));
    }

    @Override
    protected int doMatchIndex(String[] parameterNames, Class<?>[] parameterTypes, ArgumentDefinition argumentDefinition) {
        for (int i = 0; i < parameterNames.length; i++) {
            if (Pattern.matches(argumentDefinition.getIdentifier().toString(), parameterNames[i])) {
                return i;
            }
        }
        return -1;
    }
}
