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
        // 根据API接口需要匹配贷款方产品id，并放置上下文数据
        if (StringUtils.hasLength(orderId)) {
            lightCache.set(orderId);
        }
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        // 此处可以不写，因为对于一级缓存的缓存池来说不需要回收，这里调用remove方法只是移除了引用而已，但为了保持开发中良好防对象泄露，可以调用一下
        lightCache.remove();
    }
}
