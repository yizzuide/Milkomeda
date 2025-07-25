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

package com.github.yizzuide.milkomeda.satellite;

import java.lang.annotation.*;

/**
 * Redis message listener that support type of list and stream.
 *
 * @since 4.0.0
 * @author yizzuide
 * Create at 2025/07/16 13:45
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RedisMessageListener {
    /**
     * Topic name
     * @return  message topic
     */
    String topic();

    /**
     * Batch size per fetch
     * @return batch size
     */
    int batchSize() default 10;

    /**
     * Use stream (as support at redis 5.0)
     * @return true is stream
     */
    boolean stream() default false;

    /**
     * Consumer group（only support type of stream）
     * @return group name
     */
    String group() default "";

    /**
     * Wait for the timeout when the queue is empty (ms)
     * @return 0 if not supported
     */
    int timeout() default 0;

    /**
     * Message data type
     * @return class
     */
    Class<?> messageType() default String.class;
}
