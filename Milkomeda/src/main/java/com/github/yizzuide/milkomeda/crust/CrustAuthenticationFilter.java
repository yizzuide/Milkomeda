package com.github.yizzuide.milkomeda.crust;

import com.github.yizzuide.milkomeda.universe.context.ApplicationContextHolder;
import io.jsonwebtoken.ClaimJwtException;
import io.jsonwebtoken.lang.Assert;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.RequestHeaderRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.util.DigestUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * CrustAuthenticationFilter
 * 安全拦截过滤器
 *
 * @author yizzuide
 * @since 1.14.0
 * @version 1.17.0
 * Create at 2019/11/11 17:52
 */
@Slf4j
public class CrustAuthenticationFilter extends OncePerRequestFilter {

    private RequestMatcher requiresAuthenticationRequestMatcher;
    private List<RequestMatcher> permissiveRequestMatchers;
    private AuthenticationSuccessHandler successHandler = new SavedRequestAwareAuthenticationSuccessHandler();
    private AuthenticationFailureHandler failureHandler = new SimpleUrlAuthenticationFailureHandler();

    CrustAuthenticationFilter() {
        // 配置拦截匹配请求头
        String tokenName = ApplicationContextHolder.get().getBean(CrustProperties.class).getTokenName();
        this.requiresAuthenticationRequestMatcher = new RequestHeaderRequestMatcher(tokenName);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws IOException, ServletException {
        if (!requiresAuthentication(request, response)) {
            chain.doFilter(request, response);
            return;
        }

        Authentication authResult = null;
        AuthenticationException failed = null;
        Crust crust = CrustContext.get();
        String token = crust.getToken();
        try {
            if (StringUtils.isNotBlank(token)) {
                authResult = crust.getAuthenticationFromToken();
            } else {
                failed = new InsufficientAuthenticationException("Require Token is not set");
            }
        } catch (ClaimJwtException e) {
            log.error("JWT format error: {}", e.getMessage(), e);
            failed = new InsufficientAuthenticationException("JWT format error", failed);
        } catch (InternalAuthenticationServiceException e) {
            log.error("An internal error occurred while trying to authenticate the user.", failed);
            failed = e;
        } catch (AuthenticationException e) {
            // Authentication failed			
            failed = e;
        }

        if (authResult != null) {
            successfulAuthentication(request, response, chain, authResult);
        } else if (!permissiveRequest(request)) {
            unsuccessfulAuthentication(request, response, failed);
            return;
        }

        // 设置超级缓存
        crust.getCache().set(DigestUtils.md5DigestAsHex(token.getBytes()));
        chain.doFilter(request, response);
        // 响应完成后清除超级缓存
        crust.getCache().remove();
    }

    protected void unsuccessfulAuthentication(HttpServletRequest request,
                                              HttpServletResponse response, AuthenticationException failed)
            throws IOException, ServletException {
        SecurityContextHolder.clearContext();
        failureHandler.onAuthenticationFailure(request, response, failed);
    }

    protected void successfulAuthentication(HttpServletRequest request,
                                            HttpServletResponse response, FilterChain chain, Authentication authResult)
            throws IOException, ServletException {
        SecurityContextHolder.getContext().setAuthentication(authResult);
        successHandler.onAuthenticationSuccess(request, response, authResult);
    }

    protected boolean requiresAuthentication(HttpServletRequest request,
                                             HttpServletResponse response) {
        return requiresAuthenticationRequestMatcher.matches(request);
    }

    protected boolean permissiveRequest(HttpServletRequest request) {
        if (permissiveRequestMatchers == null)
            return false;
        for (RequestMatcher permissiveMatcher : permissiveRequestMatchers) {
            if (permissiveMatcher.matches(request))
                return true;
        }
        return false;
    }

    public void setPermissiveUrl(String... urls) {
        if (permissiveRequestMatchers == null)
            permissiveRequestMatchers = new ArrayList<>();
        for (String url : urls)
            permissiveRequestMatchers.add(new AntPathRequestMatcher(url));
    }

    public void setAuthenticationSuccessHandler(
            AuthenticationSuccessHandler successHandler) {
        Assert.notNull(successHandler, "successHandler cannot be null");
        this.successHandler = successHandler;
    }

    public void setAuthenticationFailureHandler(
            AuthenticationFailureHandler failureHandler) {
        Assert.notNull(failureHandler, "failureHandler cannot be null");
        this.failureHandler = failureHandler;
    }
}
