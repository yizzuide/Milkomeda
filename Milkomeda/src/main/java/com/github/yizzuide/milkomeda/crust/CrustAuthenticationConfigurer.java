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
import org.springframework.security.config.annotation.web.HttpSecurityBuilder;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.function.Supplier;

/**
 * CrustAuthenticationConfigurer
 * 认证配置器
 *
 * @author yizzuide
 * @since 1.14.0
 * @version 3.14.0
 * <br>
 * Create at 2019/11/12 22:26
 */
public class CrustAuthenticationConfigurer<T extends CrustAuthenticationConfigurer<T, B>, B extends HttpSecurityBuilder<B>> extends AbstractHttpConfigurer<T, B> {
    // 认证过滤器
    private final CrustAuthenticationFilter authFilter;
    // 认证失败处理器
    private final Supplier<AuthenticationFailureHandler> authFailureHandler;

    public CrustAuthenticationConfigurer() {
        this(null);
    }

    public CrustAuthenticationConfigurer(Supplier<AuthenticationFailureHandler> authFailureHandler) {
        this.authFilter = new CrustAuthenticationFilter();
        this.authFailureHandler = authFailureHandler;
    }

    @Override
    public void configure(B http) {
        // 设置认证失败处理器
        if (this.authFailureHandler != null) {
            authFilter.setAuthenticationFailureHandler(authFailureHandler.get());
        }
        CrustProperties crustProperties = ApplicationContextHolder.get().getBean(CrustProperties.class);
        if (crustProperties.isEnableAutoRefreshToken()) {
            authFilter.setAuthenticationSuccessHandler(new RefreshSuccessHandler(crustProperties.getRefreshTokenName()));
        }

        CrustAuthenticationFilter filter = postProcess(authFilter);
        // token验证过滤器
        http.addFilterBefore(filter, UsernamePasswordAuthenticationFilter.class);
    }

    /**
     * 设置忽略的URL
     * @param urls  请求URL列表
     * @return CrustAuthenticationConfigurer
     */
    CrustAuthenticationConfigurer<T, B> permissiveRequestUrls(String... urls) {
        authFilter.setPermissiveUrl(urls);
        return this;
    }
}

/**
 * 授权成功处理器 --转向--> 刷新Token处理器
 */
class RefreshSuccessHandler implements AuthenticationSuccessHandler {
    // token刷新间隔
    private final long tokenRefreshInterval;
    // token刷新响应字段
    private final String refreshTokenName;

    RefreshSuccessHandler(String refreshTokenName) {
        tokenRefreshInterval = ApplicationContextHolder.get().getBean(CrustProperties.class)
                .getRefreshTokenInterval().toMinutes();
        this.refreshTokenName = refreshTokenName;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) {
        boolean shouldRefresh = shouldTokenRefresh(CrustContext.get().getTokenExpire());
        if (shouldRefresh) {
            response.setHeader(refreshTokenName, CrustContext.get().refreshToken());
        }
    }

    private boolean shouldTokenRefresh(long expire) {
        LocalDateTime expireTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(expire), ZoneId.systemDefault());
        // token过期时间 - token刷新间隔秒数 < 当前时间
        return LocalDateTime.now().isAfter(expireTime.minusMinutes(tokenRefreshInterval));
    }
}
