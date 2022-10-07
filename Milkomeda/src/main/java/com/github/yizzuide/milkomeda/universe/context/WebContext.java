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

package com.github.yizzuide.milkomeda.universe.context;

import org.springframework.util.PathMatcher;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.util.UrlPathHelper;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * WebContext
 *
 * @author yizzuide
 * @since 1.14.0
 * @version 3.13.0
 * <br />
 * Create at 2019/11/11 21:38
 */
public final class WebContext {

    /**
     * 路径匹配器
     */
    private static PathMatcher mvcPathMatcher;

    /**
     * URL路径帮助类
     */
    private static UrlPathHelper urlPathHelper;

    public static void setMvcPathMatcher(PathMatcher mvcPathMatcher) {
        WebContext.mvcPathMatcher = mvcPathMatcher;
    }

    /**
     * 路径匹配器
     * @return  PathMatcher
     */
    public static PathMatcher getMvcPathMatcher() {
        return mvcPathMatcher;
    }

    public static void setUrlPathHelper(UrlPathHelper urlPathHelper) {
        WebContext.urlPathHelper = urlPathHelper;
    }

    /**
     * 请求路径帮助类
     * @return  UrlPathHelper
     */
    public static UrlPathHelper getUrlPathHelper() {
        return urlPathHelper;
    }

    /**
     * 获取请求信息
     * @return  ServletRequestAttributes
     */
    public static ServletRequestAttributes getRequestAttributes() {
        return (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
    }

    /**
     * 获得请求对象
     * @return  HttpServletRequest
     */
    public static HttpServletRequest getRequest() {
        return getRequestAttributes().getRequest();
    }

    /**
     * 获取响应对象
     * @return HttpServletResponse
     */
    public static HttpServletResponse getResponse() {
        return getRequestAttributes().getResponse();
    }

    /**
     * 获取当前会话
     * @return  HttpSession
     */
    public static HttpSession getSession() {
        return getRequest().getSession();
    }
}
