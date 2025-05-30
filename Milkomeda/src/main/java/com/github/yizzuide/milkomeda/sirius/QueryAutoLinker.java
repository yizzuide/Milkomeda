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

package com.github.yizzuide.milkomeda.sirius;

import java.lang.annotation.*;

/**
 * Auto generate multi {@link QueryLinker}.
 *
 * @since 3.15.0
 * @version 3.20.0
 * @author yizzuide
 * Create at 2023/09/21 10:33
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.ANNOTATION_TYPE})
@Inherited
@Repeatable(QueryAutoLinkers.class)
public @interface QueryAutoLinker {

    /**
     * Link expression.
     * @return link expression
     */
    String links();

    /**
     * Reference id field name.
     * @return field name
     */
    String linkIdField() default "id";

    /**
     * Bind link expression in query group.
     * @return  group name
     */
    String[] group() default { IPageableService.DEFAULT_GROUP };

    /**
     * Mapping fields from target to linker.
     * @return mapping expression
     */
    String mappings() default "";

    /**
     * Link entity class.
     * @return mapper class
     */
    Class<?> type();
}
