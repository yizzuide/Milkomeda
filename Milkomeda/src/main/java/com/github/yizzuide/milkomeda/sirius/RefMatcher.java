/*
 * Copyright (c) 2022 yizzuide All rights Reserved.
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

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import java.lang.annotation.*;

/**
 * Reference matcher using for {@link IPageableService}.
 *
 * @since 3.14.0
 * @author yizzuide
 * <br>
 * Create at 2022/11/10 21:55
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.TYPE, ElementType.ANNOTATION_TYPE})
public @interface RefMatcher {
    /**
     * Reference type.
     * @return  self type by default
     */
    RefType type() default RefType.SELF;

    /**
     * Foreign field referenced at, must add on mapper type.
     * @return reference field
     */
    String foreignField() default "";

    /**
     * Foreign mapper class, must add on mapper type.
     * @return mapper class
     */
    Class<?> foreignMapper() default BaseMapper.class;

    enum RefType {
        /**
         * Referenced by self, used on entity field.
         */
        SELF,

        /**
         * Foreign Reference, used on mapper type.
         */
        FOREIGN
    }
}
