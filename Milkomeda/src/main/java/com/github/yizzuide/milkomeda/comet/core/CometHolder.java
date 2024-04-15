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

import com.github.yizzuide.milkomeda.comet.collector.CometCollectorProperties;
import com.github.yizzuide.milkomeda.comet.logger.CometLoggerProperties;
import com.github.yizzuide.milkomeda.universe.context.WebContext;
import com.github.yizzuide.milkomeda.universe.parser.url.URLPathMatcher;
import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;

import java.util.List;

/**
 * CometHolder
 *
 * @author yizzuide
 * @since 3.0.0
 * @version 3.15.0
 * <br>
 * Create at 2020/03/28 12:42
 */
public class CometHolder {
    private static CometProperties props;

    private static CometLoggerProperties logProps;

    private static CometCollectorProperties collectorProps;

    private static List<CometRequestInterceptor> requestInterceptors;

    private static List<CometResponseInterceptor> responseInterceptors;

    static void setProps(CometProperties props) {
        CometHolder.props = props;
    }

    static CometProperties getProps() {
        return props;
    }

    public static void setCollectorProps(CometCollectorProperties collectorProps) {
        CometHolder.collectorProps = collectorProps;
    }

    static CometCollectorProperties getCollectorProps() {
        return collectorProps;
    }

    public static void setLogProps(CometLoggerProperties logProps) {
        CometHolder.logProps = logProps;
    }

    static CometLoggerProperties getLogProps() {
        return logProps;
    }

    static void setRequestInterceptors(List<CometRequestInterceptor> requestInterceptors) {
        CometHolder.requestInterceptors = requestInterceptors;
    }

    static List<CometRequestInterceptor> getRequestInterceptors() {
        return requestInterceptors;
    }

    static void setResponseInterceptors(List<CometResponseInterceptor> responseInterceptors) {
        CometHolder.responseInterceptors = responseInterceptors;
    }

    static List<CometResponseInterceptor> getResponseInterceptors() {
        return responseInterceptors;
    }

    /**
     * 是否需要包装请求
     * @return 请求数据是否要被缓存起来采集
     * @since 3.11.0
     */
    public static boolean shouldWrapRequest() {
        return shouldWrapRequest(WebContext.getRequest());
    }

    public static boolean shouldWrapRequest(ServletRequest request) {
        if (request instanceof HttpServletRequest) {
            HttpServletRequest req = (HttpServletRequest) request;
            if (URLPathMatcher.match(getProps().getExcludeUrls(), req.getRequestURI())) {
                return false;
            }
        }
        return getProps().isEnableReadRequestBody();
    }

    /**
     * 是否需要包装响应
     * @return  响应是否要被缓存起来采集
     * @since 3.11.0
     */
    public static boolean shouldWrapResponse() {
        return shouldWrapResponse(WebContext.getRequest());
    }

    public static boolean shouldWrapResponse(ServletRequest request) {
        if (request instanceof HttpServletRequest) {
            HttpServletRequest req = (HttpServletRequest) request;
            if (URLPathMatcher.match(getProps().getExcludeUrls(), req.getRequestURI())) {
                return false;
            }
        }
        return getProps().isEnableReadResponseBody();
    }

    /**
     * 响应可否可读
     * @return  响应数据是否可以被探测
     * @since 3.11.0
     */
    public static boolean isResponseReadable() {
        return  (getCollectorProps() != null && getCollectorProps().isEnableTag()) ||
                (getLogProps() != null && getLogProps().isEnableResponse());
    }
}
