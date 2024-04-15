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

/**
 * Common using impl of Argument matcher has recognized name and type with {@link ArgumentDefinition}.
 *
 * @since 3.15.0
 * @author yizzuide
 * <br>
 * Create at 2023/01/11 19:29
 */
public class NamedTypeArgumentMatcher extends AbstractArgumentMatcher {
    @Override
    public boolean support(ArgumentDefinition argumentDefinition) {
        ArgumentMatchType type = argumentDefinition.getMatchType();
        if (type == ArgumentMatchType.BY_TYPE) {
            return argumentDefinition.getArgClassRef() != null;
        }
        if (type == ArgumentMatchType.BY_NAME_CONTAINS ||
                type == ArgumentMatchType.BY_NAME_PREFIX ||
                type == ArgumentMatchType.BY_NAME_POSTFIX) {
            return argumentDefinition.getIdentifier() != null && !StringExtensionsKt.isEmpty(argumentDefinition.getIdentifier().toString());
        }
        return false;
    }

    @Override
    public int doMatchIndex(String[] parameterNames, Class<?>[] parameterTypes, ArgumentDefinition argumentDefinition) {
        // by type
        if (argumentDefinition.getMatchType() == ArgumentMatchType.BY_TYPE) {
            for (int i = 0; i < parameterTypes.length; i++) {
                Class<?> argClass = argumentDefinition.getArgClassRef().get();
                if (argClass != null && argClass.equals(parameterTypes[i])) {
                    return i;
                }
            }
            return -1;
        }
        // by name
        for (int i = 0; i < parameterNames.length; i++) {
            String name = parameterNames[i];
            switch (argumentDefinition.getMatchType()) {
                case BY_NAME_PREFIX: {
                    if (name.startsWith(String.valueOf(argumentDefinition.getIdentifier()))) {
                        return i;
                    }
                    break;
                }
                case BY_NAME_POSTFIX: {
                    if (name.endsWith(String.valueOf(argumentDefinition.getIdentifier()))) {
                        return i;
                    }
                    break;
                }
                case BY_NAME_CONTAINS: {
                    if (name.contains(String.valueOf(argumentDefinition.getIdentifier()))) {
                        return i;
                    }
                    break;
                }
            }
        }
        return -1;
    }
}
