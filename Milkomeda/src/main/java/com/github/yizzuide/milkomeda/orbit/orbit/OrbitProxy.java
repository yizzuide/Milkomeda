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

package com.github.yizzuide.milkomeda.orbit.orbit;

import com.github.yizzuide.milkomeda.orbit.Orbit;
import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.*;

/**
 * The default proxy for enhance implementation annotation of {@link Orbit}. It is accomplished through method proxy processing
 * together with {@link OrbitHandler} and {@link OrbitAround}.
 *
 * @since 3.21.0
 * @author yizzuide
 * Create at 2025/05/18 15:03
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
@Inherited
public @interface OrbitProxy {
    /**
     * Corresponding to {@link OrbitAround} value.
     * @return around tag
     */
    @AliasFor("tag")
    String value() default "";

    /**
     * Corresponding to {@link OrbitAround} tag.
     * @return around tag
     */
    @AliasFor("value")
    String tag() default "";
}
