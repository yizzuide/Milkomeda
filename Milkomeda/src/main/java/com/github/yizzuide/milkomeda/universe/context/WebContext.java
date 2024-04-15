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

import com.github.yizzuide.milkomeda.comet.core.CometResponseWrapper;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import lombok.Getter;
import lombok.Setter;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import org.springframework.util.PathMatcher;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.util.WebUtils;
import org.springframework.web.util.pattern.PathPatternParser;

/**
 * Spring web context.
 *
 * @author yizzuide
 * @since 1.14.0
 * @version 3.20.0
 * <br>
 * Create at 2019/11/11 21:38
 */
public final class WebContext {

    @Setter @Getter
    private static PathMatcher mvcPathMatcher;

    @Setter @Getter
    private static PathPatternParser mvcPatternParser;

    /**
     * 获取请求信息
     * @return  ServletRequestAttributes
     */
    @Nullable
    public static ServletRequestAttributes getRequestAttributes() {
        return (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
    }

    /**
     * 获得请求对象
     * @return  HttpServletRequest
     */
    @Nullable
    public static HttpServletRequest getRequest() {
        ServletRequestAttributes requestAttributes = getRequestAttributes();
        if (requestAttributes == null) return null;
        return requestAttributes.getRequest();
    }

    @NonNull
    public static HttpServletRequest getRequestNonNull() {
        HttpServletRequest request = WebContext.getRequest();
        Assert.notNull(request, "HttpServletRequest cannot be null");
        return request;
    }

    /**
     * 获取响应对象
     * @return HttpServletResponse
     */
    @Nullable
    public static HttpServletResponse getResponse() {
        ServletRequestAttributes requestAttributes = getRequestAttributes();
        if (requestAttributes == null) return null;
        return requestAttributes.getResponse();
    }

    /**
     * Get servlet response whenever had wrapped.
     * @return unwrapped response
     * @since 3.15.0
     */
    public static HttpServletResponse getRawResponse() {
        HttpServletResponse response = getResponse();
        assert response != null;
        CometResponseWrapper responseWrapper =
                WebUtils.getNativeResponse(response, CometResponseWrapper.class);
        if (responseWrapper == null) {
            return response;
        }
        return (HttpServletResponse) responseWrapper.getResponse();
    }

    /**
     * 获取当前会话
     * @return  HttpSession
     */
    @Nullable
    public static HttpSession getSession() {
        HttpServletRequest request = getRequest();
        if (request == null) return null;
        return request.getSession();
    }
}
