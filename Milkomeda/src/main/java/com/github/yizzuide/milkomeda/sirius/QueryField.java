/*
 * Copyright (c) 2025 yizzuide All rights Reserved.
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

package com.github.yizzuide.milkomeda.sirius;

import java.lang.annotation.*;

/**
 * Custom query match with entity.
 *
 * @author yizzuide
 * @since 4.0.0
 * Create at 2025/07/03 23:21
 */
@Documented
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface QueryField {

    /**
     * mapping query entity field name or used to match data (only support with `Query` object).
     * @return default is the same name
     */
    String value() default "";

    /**
     * match field for used in `queryFields` of {@link QueryMatcher} (only support with `Query` object).
     * @return true if for used
     */
    boolean matched() default false;

    /**
     * The field is included in query result.
     * @return true if included
     */
    boolean include() default false;

    /**
     * The field is excluded in query result.
     * @return true if excluded
     */
    boolean exclude() default false;

    /**
     * select in a group.
     * @return  group name
     */
    String[] group() default {"default"};
}
