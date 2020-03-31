package com.github.yizzuide.milkomeda.hydrogen.interceptor;

import lombok.Data;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.MappedInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * HydrogenMappedInterceptor
 * 装饰模式增强的MappedInterceptor
 *
 * @author yizzuide
 * @since 3.0.0
 * Create at 2020/03/31 14:56
 */
@Data
public class HydrogenMappedInterceptor implements HandlerInterceptor {

    // 由于MappedInterceptor被final修饰，所以使用继承方式无解
    private MappedInterceptor mappedInterceptor;

    /**
     * 排序
     */
    private int order = 0;

    public HydrogenMappedInterceptor(MappedInterceptor mappedInterceptor) {
        this.mappedInterceptor = mappedInterceptor;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        return mappedInterceptor.preHandle(request, response, handler);
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        mappedInterceptor.postHandle(request, response, handler, modelAndView);
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        mappedInterceptor.afterCompletion(request, response, handler, ex);
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof HandlerInterceptor)) {
            return false;
        }
        if (obj instanceof HydrogenMappedInterceptor) {
            return this.getMappedInterceptor().getInterceptor().getClass() ==
                    ((HydrogenMappedInterceptor) obj).getMappedInterceptor().getInterceptor().getClass();
        }
        // HandlerInterceptor
        return this.getMappedInterceptor().getInterceptor().getClass() == obj.getClass();
    }
}
