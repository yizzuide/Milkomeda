package com.github.yizzuide.milkomeda.comet.core;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

/**
 * CometRequestFilter
 * 请求过滤器
 *
 * @author yizzuide
 * @since 2.0.0
 * Create at 2019/12/12 17:48
 */
public class CometRequestFilter implements Filter {

    @Override
    public void init(FilterConfig filterConfig) {
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        ServletRequest requestWrapper = servletRequest;
        // 如果有Form表单数据则不读取body，交给SpringMVC框架处理（但@CometParam功能仍然有效）
        if (!servletRequest.getParameterNames().hasMoreElements()) {
            requestWrapper = new CometRequestWrapper((HttpServletRequest) servletRequest);
        }
        filterChain.doFilter(requestWrapper, servletResponse);
        // 清空数据
        CometAspect.resolveThreadLocal.remove();
    }

    @Override
    public void destroy() {
    }
}
