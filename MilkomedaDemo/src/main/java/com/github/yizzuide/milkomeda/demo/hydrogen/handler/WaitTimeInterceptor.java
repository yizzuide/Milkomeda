package com.github.yizzuide.milkomeda.demo.hydrogen.handler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * WaitTimeInterceptor
 *
 * @author yizzuide
 * Create at 2020/03/31 14:17
 */
@Slf4j
public class WaitTimeInterceptor extends HandlerInterceptorAdapter {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        request.setAttribute("wt.startTime", System.currentTimeMillis());
        return super.preHandle(request, response, handler);
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        long startTime = (long) request.getAttribute("wt.startTime");
        log.info("take time: {}ms", System.currentTimeMillis() - startTime);
        super.afterCompletion(request, response, handler, ex);
    }
}
