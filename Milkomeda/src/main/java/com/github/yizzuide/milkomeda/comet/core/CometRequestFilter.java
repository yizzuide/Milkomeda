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

import com.github.yizzuide.milkomeda.util.IdGenerator;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.web.util.WebUtils;

import java.io.IOException;

/**
 * 请求过滤器
 *
 * @see org.springframework.web.filter.CharacterEncodingFilter
 * @see org.apache.coyote.Response#isCommitted()
 * @author yizzuide
 * @since 2.0.0
 * @version 3.5.0
 * <br>
 * Create at 2019/12/12 17:48
 */
@Slf4j
public class CometRequestFilter implements Filter {

    private static final String COMET_REQ_ID = "COMET.REQ_ID";

    @Override
    public void init(FilterConfig filterConfig) {
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        // 设置编码，防止Spring MVC注册Filter顺序问题导致乱码问题（目前已经保证Spring Web MVC的CharacterEncodingFilter优先设置）
        // servletRequest.setCharacterEncoding(Charset.defaultCharset().toString());
        ServletRequest requestWrapper = servletRequest;
        if (CometHolder.shouldWrapRequest()) {
            // 如果有Form表单数据则不读取body，交给SpringMVC框架处理（但@CometParam功能仍然有效）
            boolean cacheBody = CollectionUtils.isEmpty(servletRequest.getParameterMap());
            requestWrapper = new CometRequestWrapper((HttpServletRequest) servletRequest, cacheBody);
        }
        boolean enableAddResponseWrapper = CometHolder.shouldWrapResponse();
        if (enableAddResponseWrapper) {
            servletResponse = new CometResponseWrapper((HttpServletResponse) servletResponse);
        }
        MDC.put(COMET_REQ_ID, IdGenerator.genNext32ID());
        filterChain.doFilter(requestWrapper, servletResponse);
        MDC.remove(COMET_REQ_ID);
        // 更新响应消息体
        if (enableAddResponseWrapper) {
            updateResponse((HttpServletResponse) servletResponse);
        }
        // 清空线程数据
        CometAspect.resolveThreadLocal.remove();
    }

    private void updateResponse(HttpServletResponse response) throws IOException {
        CometResponseWrapper responseWrapper =
                WebUtils.getNativeResponse(response, CometResponseWrapper.class);
        Assert.notNull(responseWrapper, "CometResponseWrapper not found");
        // HttpServletResponse rawResponse = (HttpServletResponse) responseWrapper.getResponse();
        responseWrapper.copyBodyToResponse();
    }

    @Override
    public void destroy() {
    }
}
