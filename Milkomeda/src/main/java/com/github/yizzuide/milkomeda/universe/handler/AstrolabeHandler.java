package com.github.yizzuide.milkomeda.universe.handler;

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
