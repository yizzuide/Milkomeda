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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * A argument definition list to ensure correct order.
 *
 * @since 3.15.0
 * @author yizzuide
 * <br>
 * Create at 2023/01/11 19:52
 */
public class ArgumentSources {

    private final List<ArgumentDefinition> argsDefinitionSource;

    public ArgumentSources() {
        argsDefinitionSource = new ArrayList<>();
    }

    public ArgumentSources(ArgumentDefinition ...argumentDefinition) {
        this();
        argsDefinitionSource.addAll(Arrays.asList(argumentDefinition));
    }

    public ArgumentSources add(ArgumentDefinition argumentDefinition) {
        argsDefinitionSource.add(argumentDefinition);
        return this;
    }

    public List<ArgumentDefinition> getList() {
        return argsDefinitionSource;
    }
}
