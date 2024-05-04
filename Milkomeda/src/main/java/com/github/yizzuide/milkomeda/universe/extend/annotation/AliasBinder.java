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

package com.github.yizzuide.milkomeda.universe.extend.annotation;

import com.github.yizzuide.milkomeda.comet.core.CometResponseInterceptor;

import java.lang.annotation.*;

/**
 * Add alias bind to bean, but this name can not find in spring context (so you can have duplicate names in other modules).
 * It is configured in the list of {@link CometResponseInterceptor}.
 *
 * @since 3.15.0
 * @version 3.20.0
 * @author yizzuide
 * <br>
 * Create at 2023/05/05 21:54
 */
@Documented
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface AliasBinder {
    /**
     * Set bean alias name.
     * @return bean alias name.
     */
    String value();

    /**
     * Whether to load automatically.
     * @return false is not
     * @since 3.20.0
     */
    boolean autoload() default false;
}
