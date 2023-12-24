package com.github.yizzuide.milkomeda.demo.hydrogen.handler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.web.servlet.AsyncHandlerInterceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * WaitTimeInterceptor
 *
 * @author yizzuide
 * <br>
 * Create at 2020/03/31 14:17
 */
@Slf4j
public class WaitTimeInterceptor implements AsyncHandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull Object handler) throws Exception {
        request.setAttribute("wt.startTime", System.currentTimeMillis());
        return AsyncHandlerInterceptor.super.preHandle(request, response, handler);
    }

    @Override
    public void afterCompletion(HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull Object handler, Exception ex) throws Exception {
        long startTime = (long) request.getAttribute("wt.startTime");
        log.info("take time: {}ms", System.currentTimeMillis() - startTime);
        AsyncHandlerInterceptor.super.afterCompletion(request, response, handler, ex);
    }
}
