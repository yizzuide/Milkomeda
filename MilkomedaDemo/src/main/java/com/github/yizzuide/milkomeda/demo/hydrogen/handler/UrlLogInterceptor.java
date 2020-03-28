package com.github.yizzuide.milkomeda.demo.hydrogen.handler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * UrlLogInterceptor
 *
 * @author yizzuide
 * Create at 2020/03/28 01:08
 */
@Slf4j
public class UrlLogInterceptor extends HandlerInterceptorAdapter implements InitializingBean {
    // 动态注册的拦截器，不需要在提前类上添加@Component注解也可以注入
    @Autowired
    private MessageSource messageSource;
    // 日志开关
    private boolean open;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (open) {
            log.info("url-> {}", request.getRequestURI());
        }
        return super.preHandle(request, response, handler);
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        log.info("UrlLogInterceptor register success");
    }
}
