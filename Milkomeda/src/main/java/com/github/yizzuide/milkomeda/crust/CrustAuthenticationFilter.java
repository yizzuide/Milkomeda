package com.github.yizzuide.milkomeda.crust;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * CrustAuthenticationFilter
 * 安全拦截过滤器
 *
 * @author yizzuide
 * @since 1.14.0
 * Create at 2019/11/11 17:52
 */
public class CrustAuthenticationFilter extends BasicAuthenticationFilter {

    @Autowired
    public CrustAuthenticationFilter(AuthenticationManager authenticationManager) {
        super(authenticationManager);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws IOException, ServletException {
        // 检查并认证token
        CrustContext.get().checkAuthentication();
        chain.doFilter(request, response);
    }
}
