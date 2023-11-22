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

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.lang.ref.WeakReference;

/**
 * Argument metadata used with {@link ArgumentSources}.
 *
 * @since 3.15.0
 * @author yizzuide
 * <br>
 * Create at 2023/01/11 19:42
 */
@AllArgsConstructor
@NoArgsConstructor
@Data
public class ArgumentDefinition {
    /**
     * The match type is used to match the processor.
     */
    private ArgumentMatchType matchType;

    /**
     * Custom type is used for extent argument matcher.
     */
    private String customTypeName;

    /**
     * Argument identifier (such as param name etc.).
     */
    private Object identifier;

    /**
     * Class type of argument value.
     */
    private WeakReference<? extends Class<?>> argClassRef;

    /**
     * Argument value which need invoke.
     */
    private Object value;

    public ArgumentDefinition(ArgumentMatchType matchType, Object identifier, Class<?> argClass, Object value) {
        this.matchType = matchType;
        this.identifier = identifier;
        this.argClassRef = new WeakReference<>(argClass);
        this.value = value;
    }

    public ArgumentDefinition(String customTypeName, Object identifier, Class<?> argClass, Object value) {
        this.customTypeName = customTypeName;
        this.identifier = identifier;
        this.argClassRef = new WeakReference<>(argClass);
        this.value = value;
    }
}
