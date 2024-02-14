/*
 * Copyright (c) 2021 yizzuide All rights Reserved.
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

package com.github.yizzuide.milkomeda.comet.core;

import com.github.yizzuide.milkomeda.orbit.OrbitRegistrar;
import com.github.yizzuide.milkomeda.wormhole.WormholeEventBus;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * The comet bossiness log record which combines with the {@link OrbitRegistrar} and {@link WormholeEventBus}.
 *
 * @author yizzuide
 * @since 1.12.0
 * @version 4.0.0
 * <br>
 * Create at 2019/09/21 01:15
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface CometX {
    /**
     * Label name.
     * @return String
     */
    String name();

    /**
     * Executes path.
     * @return String
     */
    String path() default "";

    /**
     * Business category.
     * @return String
     */
    String tag();

    /**
     * Subject id which relative to the {@link CometX#tag}.
     * @return Spring EL
     */
    String subjectId() default "";

    /**
     * Execute success info.
     * @return Spring EL
     */
    String success() default "";

    /**
     * Other useful info.
     * @return Spring El
     */
    String detail() default "";

    /**
     * whether to execute.
     * @return  Spring EL
     */
    String condition() default "";
}
