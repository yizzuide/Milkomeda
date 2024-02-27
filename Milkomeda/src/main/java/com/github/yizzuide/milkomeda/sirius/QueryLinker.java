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
 * Query linker using for {@link IPageableService}.
 *
 * @since 3.15.0
 * @author yizzuide
 * <br>
 * Create at 2023/06/07 01:58
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.ANNOTATION_TYPE})
@Inherited
@Repeatable(QueryLinkers.class)
public @interface QueryLinker {
    /**
     * Target field for set name.
     * @return set field
     */
    String targetNameField();

    /**
     * Link name field for {@link QueryLinker#targetNameField}.
     * @return link name
     */
    String linkNameField();

    /**
     * Reference id field name.
     * @return field name
     */
    String linkIdField() default "id";

    /**
     * Link id value should ignore.
     * @return id value
     */
    String linkIdIgnore() default "0";

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

    /**
     * Bind conditions in query group.
     * @return  group name
     */
    String[] group() default { IPageableService.DEFAULT_GROUP };
}
