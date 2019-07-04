package com.github.yizzuide.milkomeda.demo.light.handler;

import com.github.yizzuide.milkomeda.demo.light.pojo.Order;
import com.github.yizzuide.milkomeda.light.Cache;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * RequestInterceptor
 *
 * @author yizzuide
 * Create at 2019/07/02 10:48
 */
@Component
public class RequestInterceptor implements HandlerInterceptor {

    @Resource
    private Cache<String, Order> lightCache;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String orderId = request.getParameter("orderId");
        // 如果有订单id参数，放置上下文数据
        if (StringUtils.hasLength(orderId)) {
            lightCache.set(orderId);
        }
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        // 移除超级缓存数据，防止泄露
        lightCache.remove();
    }
}
