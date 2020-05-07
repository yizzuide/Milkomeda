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
 * @version 2.0.4
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
        authFilter.setAuthenticationFailureHandler(authFailureHandler.get());
        CrustProperties crustProperties = ApplicationContextHolder.get().getBean(CrustProperties.class);
        if (crustProperties.isEnableAutoRefreshToken()) {
            authFilter.setAuthenticationSuccessHandler(new RefreshSuccessHandler(crustProperties.getRefreshTokenName()));
        }

        CrustAuthenticationFilter filter = postProcess(authFilter);
        // token验证过滤器
        http.addFilterBefore(filter, UsernamePasswordAuthenticationFilter.class);
    }

    CrustAuthenticationConfigurer<T, B> permissiveRequestUrls(String... urls) {
        authFilter.setPermissiveUrl(urls);
        return this;
    }
}

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
