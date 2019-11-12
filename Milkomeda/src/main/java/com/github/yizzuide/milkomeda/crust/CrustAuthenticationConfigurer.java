package com.github.yizzuide.milkomeda.crust;

import com.github.yizzuide.milkomeda.universe.context.ApplicationContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.HttpSecurityBuilder;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.logout.LogoutFilter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

/**
 * CrustAuthenticationConfigurer
 * 认证配置器
 *
 * @author yizzuide
 * @since 1.14.0
 * Create at 2019/11/12 22:26
 */
public class CrustAuthenticationConfigurer<T extends CrustAuthenticationConfigurer<T, B>, B extends HttpSecurityBuilder<B>> extends AbstractHttpConfigurer<T, B> {
    private CrustAuthenticationFilter authFilter;

    CrustAuthenticationConfigurer() {
        this.authFilter = new CrustAuthenticationFilter();
    }

    @Override
    public void configure(B http) {
        // 设置认证失败处理器
        authFilter.setAuthenticationFailureHandler(((request, response, exception) -> response.setStatus(HttpStatus.UNAUTHORIZED.value())));
        if (ApplicationContextHolder.get().getBean(CrustProperties.class).isEnableAutoRefreshToken()) {
            authFilter.setAuthenticationSuccessHandler(new RefreshSuccessHandler());
        }

        CrustAuthenticationFilter filter = postProcess(authFilter);
        // token验证过滤器
//        http.addFilterAfter(new CrustAuthenticationFilter(), AbstractPreAuthenticatedProcessingFilter.class);
        http.addFilterBefore(filter, LogoutFilter.class);
    }

    CrustAuthenticationConfigurer<T, B> permissiveRequestUrls(String... urls) {
        authFilter.setPermissiveUrl(urls);
        return this;
    }
}

class RefreshSuccessHandler implements AuthenticationSuccessHandler {
    // token刷新间隔
    private int tokenRefreshInterval;

    RefreshSuccessHandler() {
        tokenRefreshInterval = ApplicationContextHolder.get().getBean(CrustProperties.class)
                .getRefreshTokenInterval() * 60;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) {
        boolean shouldRefresh = shouldTokenRefresh(CrustContext.get().getTokenIssue());
        if (shouldRefresh) {
            response.setHeader("Authorization", CrustContext.get().refreshToken());
        }
    }

    private boolean shouldTokenRefresh(long issueAt) {
        LocalDateTime issueTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(issueAt), ZoneId.systemDefault());
        return LocalDateTime.now().minusSeconds(tokenRefreshInterval).isAfter(issueTime);
    }
}
