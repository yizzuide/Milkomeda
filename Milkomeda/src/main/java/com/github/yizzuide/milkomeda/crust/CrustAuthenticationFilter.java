/*
 * Copyright (c) 2021 yizzuide All rights Reserved.
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.github.yizzuide.milkomeda.crust;

import com.github.yizzuide.milkomeda.universe.context.ApplicationContextHolder;
import io.jsonwebtoken.ClaimJwtException;
import io.jsonwebtoken.lang.Assert;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.RequestHeaderRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Parse token to authentication filter.
 *
 * @see org.springframework.security.authentication.dao.AbstractUserDetailsAuthenticationProvider
 * @see org.springframework.security.web.context.SecurityContextHolderFilter
 * @see org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter
 * @see org.springframework.security.web.authentication.www.BasicAuthenticationFilter
 * @see org.springframework.security.web.authentication.AnonymousAuthenticationFilter
 * @see org.springframework.security.web.access.ExceptionTranslationFilter
 * @see org.springframework.security.web.access.intercept.AuthorizationFilter
 * @author yizzuide
 * @since 1.14.0
 * @version 4.0.0
 * <br>
 * Create at 2019/11/11 17:52
 */
@Slf4j
public class CrustAuthenticationFilter extends OncePerRequestFilter {

    private final RequestMatcher requiresAuthenticationRequestMatcher;

    private List<RequestMatcher> permissiveRequestMatchers;

    private AuthenticationSuccessHandler successHandler = new SavedRequestAwareAuthenticationSuccessHandler();

    private AuthenticationFailureHandler failureHandler = new SimpleUrlAuthenticationFailureHandler();

    CrustAuthenticationFilter() {
        // 配置拦截匹配请求头
        String tokenName = ApplicationContextHolder.get().getBean(CrustProperties.class).getTokenName();
        this.requiresAuthenticationRequestMatcher = new RequestHeaderRequestMatcher(tokenName);
    }

    @SuppressWarnings("rawtypes")
    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull FilterChain chain) throws IOException, ServletException {
        // check match permission url, let it go next.
        if (permissiveRequest(request)) {
            chain.doFilter(request, response);
            return;
        }

        CrustUserInfo authResult = null;
        AuthenticationException failed = null;
        Crust crust = CrustContext.getDefault();
        // check request header has token
        if (!requiresAuthentication(request, response)) {
            failed = new InsufficientAuthenticationException("Required token is not set.");
        } else {
            String token = crust.getToken(true);
            try {
                if (StringUtils.isNotBlank(token)) {
                    authResult = crust.getAuthInfoFromToken(token);
                } else {
                    failed = new InsufficientAuthenticationException("Token is not exists.");
                }
            } catch (ClaimJwtException e) {
                failed = new InsufficientAuthenticationException(e.getMessage());
            } catch (AuthenticationException e) {
                // Authentication failed!
                failed = e;
            }
        }

        if (authResult != null) {
            Authentication authentication = crust.getContext().getAuthentication();
            // null if getting from cache, it's need active authentication.
            if (authentication == null) {
                crust.activeAuthentication(authResult);
                authentication = crust.getContext().getAuthentication();
            }
            successfulAuthentication(request, response, chain, authentication);
        } else {
            unsuccessfulAuthentication(request, response, failed);
            return;
        }
        chain.doFilter(request, response);
        crust.invalidate();
        // Next clear `SecurityContext` within `SecurityContextHolderFilter`
    }

    protected void unsuccessfulAuthentication(HttpServletRequest request,
                                              HttpServletResponse response, AuthenticationException failed)
            throws IOException, ServletException {
        failureHandler.onAuthenticationFailure(request, response, failed);
    }

    protected void successfulAuthentication(HttpServletRequest request,
                                            HttpServletResponse response, FilterChain chain, Authentication authentication)
            throws IOException, ServletException {
        successHandler.onAuthenticationSuccess(request, response, authentication);
    }

    protected boolean requiresAuthentication(HttpServletRequest request,
                                             HttpServletResponse response) {

        return requiresAuthenticationRequestMatcher.matches(request);
    }

    protected boolean permissiveRequest(HttpServletRequest request) {
        if (permissiveRequestMatchers == null) {
            return false;
        }
        for (RequestMatcher permissiveMatcher : permissiveRequestMatchers) {
            if (permissiveMatcher.matches(request)) {
                return true;
            }
        }
        return false;
    }

    public void setPermissiveUrl(String... urls) {
        if (permissiveRequestMatchers == null) {
            permissiveRequestMatchers = new ArrayList<>();
        }
        for (String url : urls) {
            permissiveRequestMatchers.add(new AntPathRequestMatcher(url));
        }
    }

    public void setAuthenticationSuccessHandler(AuthenticationSuccessHandler successHandler) {
        Assert.notNull(successHandler, "successHandler cannot be null");
        this.successHandler = successHandler;
    }

    public void setAuthenticationFailureHandler(AuthenticationFailureHandler failureHandler) {
        Assert.notNull(failureHandler, "failureHandler cannot be null");
        this.failureHandler = failureHandler;
    }
}
