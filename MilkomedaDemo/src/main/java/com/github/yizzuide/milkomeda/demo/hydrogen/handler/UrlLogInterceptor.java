package com.github.yizzuide.milkomeda.demo.hydrogen.handler;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.lang.NonNull;
import org.springframework.web.servlet.AsyncHandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * UrlLogInterceptor
 *
 * @author yizzuide
 * <br />
 * Create at 2020/03/28 01:08
 */
@Slf4j
public class UrlLogInterceptor implements AsyncHandlerInterceptor, InitializingBean {

    // 动态注册的拦截器，不需要在提前类上添加@Component注解也可以注入
    @Autowired
    private MessageSource messageSource;

    // 可设置属性（日志开关）
    @Setter
    private boolean open;

    @Override
    public boolean preHandle(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull Object handler) throws Exception {
        if (open) {
            log.info("当前请求：{}", request.getRequestURI());
        }
        return true;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        log.info("UrlLogInterceptor register success");
    }
}
