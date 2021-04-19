package com.github.yizzuide.milkomeda.comet.core;

import lombok.extern.slf4j.Slf4j;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.web.util.WebUtils;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * CometRequestFilter
 * 请求过滤器
 *
 * @author yizzuide
 * @since 2.0.0
 * @version 3.5.0
 * @see org.springframework.web.filter.CharacterEncodingFilter
 * @see org.apache.coyote.Response#isCommitted()
 * Create at 2019/12/12 17:48
 */
@Slf4j
public class CometRequestFilter implements Filter {

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
            if (CollectionUtils.isEmpty(servletRequest.getParameterMap())) {
                requestWrapper = new CometRequestWrapper((HttpServletRequest) servletRequest);
            }
        }
        boolean enableAddResponseWrapper = CometHolder.shouldWrapResponse();
        if (enableAddResponseWrapper) {
            servletResponse = new CometResponseWrapper((HttpServletResponse) servletResponse);
        }
        filterChain.doFilter(requestWrapper, servletResponse);
        if (enableAddResponseWrapper) {
            updateResponse((HttpServletResponse) servletResponse);
        }
        // 清空数据
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
