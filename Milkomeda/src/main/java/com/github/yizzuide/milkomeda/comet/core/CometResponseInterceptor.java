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

package com.github.yizzuide.milkomeda.comet.core;

import com.mongodb.lang.Nullable;
import org.springframework.core.Ordered;
import org.springframework.util.FastByteArrayOutputStream;

import javax.servlet.http.HttpServletResponse;

/**
 * Comet response wrapper interceptor.
 *
 * @since 3.14.0
 * @author yizzuide
 * <br>
 * Create at 2022/10/10 17:22
 */
public interface CometResponseInterceptor extends Ordered {
    /**
     * Start write content to response.
     * @param outputStream  content of response
     * @param wrapperResponse  wrapper response object
     * @param rawResponse   real response object
     * @param body  response body
     * @return  if true to interrupted content winter to response body, and the behind interceptors will not be executed.
     */
    boolean writeToResponse(FastByteArrayOutputStream outputStream, HttpServletResponse wrapperResponse, HttpServletResponse rawResponse, @Nullable Object body);
}
