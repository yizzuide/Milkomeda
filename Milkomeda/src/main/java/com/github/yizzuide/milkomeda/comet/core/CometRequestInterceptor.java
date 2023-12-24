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

package com.github.yizzuide.milkomeda.comet.core;

import com.github.yizzuide.milkomeda.universe.extend.web.handler.NamedHandler;
import org.springframework.lang.Nullable;

import jakarta.servlet.http.HttpServletRequest;

/**
 * Comet request wrapper interceptor.
 *
 * @since 3.15.0
 * @author yizzuide
 * <br>
 * Create at 2023/05/01 02:59
 */
public interface CometRequestInterceptor extends NamedHandler {
    /**
     * Invoked when read value from request.
     * @param request   HttpServletRequest
     * @param formName single form name
     * @param formValue single form value
     * @param body  request body
     * @return modify value
     */
    String readRequest(HttpServletRequest request, @Nullable String formName, @Nullable String formValue, @Nullable String body);


}
