package com.github.yizzuide.milkomeda.demo.hydrogen.handler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;

import jakarta.servlet.*;
import java.io.IOException;

/**
 * IPLimiterFilter
 * IP限制过滤器
 *
 * @author yizzuide
 * <br>
 * Create at 2020/04/02 18:14
 */
@SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
@Slf4j
public class IPLimiterFilter implements Filter {
    @Autowired
    private MessageSource messageSource;

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
      log.info("request ip: {}", request.getRemoteAddr());
      chain.doFilter(request, response);
    }
}
