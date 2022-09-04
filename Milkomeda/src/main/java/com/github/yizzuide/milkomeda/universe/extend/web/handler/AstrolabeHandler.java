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

package com.github.yizzuide.milkomeda.universe.extend.web.handler;

import org.springframework.core.Ordered;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

/**
 * AstrolabeHandler
 * 星盘处理器（轻量级请求过滤器，类似线程之上的协程）
 *
 * @author yizzuide
 * @since 3.3.0
 * @see DelegatingContextFilter
 * Create at 2020/05/06 11:38
 */
public interface AstrolabeHandler extends Ordered {
    /**
     * 请求前置
     * @param request  ServletRequest
     */
    default void preHandle(ServletRequest request) {}

    /**
     * 请求后置
     * @param request   ServletRequest
     * @param response  ServletResponse
     */
    default void postHandle(ServletRequest request, ServletResponse response) {}

    @Override
    default int getOrder() {
        return 0;
    }
}
