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

import com.github.yizzuide.milkomeda.hydrogen.uniform.ResultVO;
import com.github.yizzuide.milkomeda.hydrogen.uniform.UniformHandler;
import com.github.yizzuide.milkomeda.hydrogen.uniform.UniformResult;
import com.github.yizzuide.milkomeda.universe.context.ApplicationContextHolder;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.BeanIds;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;
import org.springframework.util.CollectionUtils;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.condition.PathPatternsRequestCondition;
import org.springframework.web.servlet.mvc.condition.PatternsRequestCondition;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;
import org.springframework.web.util.pattern.PathPattern;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Spring Security config adapter, need impl with {@link org.springframework.context.annotation.Configuration}.
 *
 * @see org.springframework.security.web.session.SessionManagementFilter
 * @see org.springframework.security.config.annotation.web.configurers.SessionManagementConfigurer
 * @author yizzuide
 * @since 1.14.0
 * @version 4.0.0
 * <br>
 * Create at 2019/11/11 18:25
 */
public abstract class CrustConfigurerAdapter {

    @Autowired
    private CrustProperties props;

    @Autowired(required = false)
    private BCryptPasswordEncoder passwordEncoder;

    @Autowired
    private ApplicationContextHolder applicationContextHolder;

    @Bean
    public AuthenticationProvider authenticationProvider(AuthenticationManagerBuilder auth) {
        // 使用继承自DaoAuthenticationProvider
        CrustAuthenticationProvider authenticationProvider = new CrustAuthenticationProvider(props, passwordEncoder);
        // 扩展配置（UserDetailsService、PasswordEncoder）
        configureProvider(authenticationProvider);
        // 验证码登录Provider
        CrustCodeAuthenticationProvider codeAuthenticationProvider = new CrustCodeAuthenticationProvider(userDetailsService());
        // 添加自定义身份验证组件
        auth.authenticationProvider(authenticationProvider)
                .authenticationProvider(codeAuthenticationProvider);
        Map<String, AuthenticationProvider> customAuthenticationProviderMap = ApplicationContextHolder.get().getBeansOfType(AuthenticationProvider.class, false, true);
        if (!CollectionUtils.isEmpty(customAuthenticationProviderMap)) {
            customAuthenticationProviderMap.values().forEach(auth::authenticationProvider);
        }
        return authenticationProvider;
    }

    @Bean(name = BeanIds.AUTHENTICATION_MANAGER)
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        List<String> allowURLs = new ArrayList<>(props.getPermitUrls());
        // 允许登录
        allowURLs.add(props.getLoginUrl());
        // 额外添加的排除项
        if (!CollectionUtils.isEmpty(props.getAdditionPermitUrls())) {
            allowURLs.addAll(props.getAdditionPermitUrls());
        }
        // 标记匿名访问
        // Find URL method map
        Map<RequestMappingInfo, HandlerMethod> handlerMethodMap = applicationContextHolder.getApplicationContext()
                .getBean(com.github.yizzuide.milkomeda.universe.metadata.BeanIds.REQUEST_MAPPING_HANDLER_MAPPING,
                        RequestMappingHandlerMapping.class).getHandlerMethods();
        Set<String> anonUrls = new HashSet<>();
        for (Map.Entry<RequestMappingInfo, HandlerMethod> infoEntry : handlerMethodMap.entrySet()) {
            HandlerMethod handlerMethod = infoEntry.getValue();
            // Has `CrustAnon` annotation on Method？
            CrustAnon crustAnon = handlerMethod.getMethodAnnotation(CrustAnon.class);
            if (null != crustAnon) {
                Collection<String> requestPatterns = null;
                PatternsRequestCondition patternsCondition = infoEntry.getKey().getPatternsCondition();
                if (patternsCondition != null) {
                    requestPatterns = patternsCondition.getPatterns();
                }
                if (requestPatterns == null) {
                    PathPatternsRequestCondition pathPatternsCondition = infoEntry.getKey().getPathPatternsCondition();
                    if (pathPatternsCondition != null) {
                        requestPatterns = pathPatternsCondition.getPatterns().stream().map(PathPattern::getPatternString).collect(Collectors.toSet());
                    }
                }
                if (requestPatterns != null) {
                    anonUrls.addAll(requestPatterns);
                }
            }
        }
        if (!CollectionUtils.isEmpty(anonUrls)) {
            allowURLs.addAll(anonUrls);
        }
        String[] permitAllMapping = allowURLs.toArray(new String[0]);
        // 通用失败处理器
        DefaultFailureHandler failureHandler = new DefaultFailureHandler(this);
        // Spring Boot 3.0：配置方式由方法链改为Spring Security lambda DSL
        http.csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(configurer -> configurer.sessionCreationPolicy(props.isStateless() ?
                        SessionCreationPolicy.STATELESS : SessionCreationPolicy.IF_REQUIRED))
                .formLogin(AbstractHttpConfigurer::disable)
                // 支持跨域，从CorsConfigurationSource中取跨域配置
                .cors(Customizer.withDefaults())
                // 禁用iframe跨域
                .headers(configurer -> configurer.frameOptions(HeadersConfigurer.FrameOptionsConfig::disable))
                .authorizeHttpRequests(registry -> registry
                        // 跨域预检请求
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        // 忽略的请求
                        .requestMatchers(permitAllMapping).permitAll()
                        // 其他所有请求需要身份认证
                        .anyRequest().authenticated()
                );


        // 如果是无状态方式
        if (props.isStateless()) {
            allowURLs.add(props.getLoginUrl());
            // 应用Token认证配置器，忽略需要匿名的请求
            http.with(new CrustAuthenticationConfigurer<>(() -> failureHandler), configurer -> configurer.permissiveRequestUrls(allowURLs.toArray(new String[0])))
                    .logout(configurer -> configurer
                        .logoutUrl(props.getLogoutUrl())
                        .addLogoutHandler((req, res, auth) -> CrustContext.invalidate())
                        .logoutSuccessHandler((req, res, auth) -> {
                            ResultVO<?> source = UniformResult.ok(null);
                            UniformHandler.matchStatusToWrite(res, source.toMap());
                        })
                    );
        } else {
            // 自定义session方式登录
            http.httpBasic(configurer -> configurer.authenticationEntryPoint(new LoginUrlAuthenticationEntryPoint(props.getLoginUrl())))
                    .sessionManagement(configurer -> configurer
                            .sessionFixation()
                            .changeSessionId()
                            .sessionAuthenticationErrorUrl(props.getLoginUrl())
                            .sessionAuthenticationFailureHandler(failureHandler))
                    .logout(configurer -> configurer
                            .logoutUrl(props.getLogoutUrl())
                            .logoutSuccessUrl(props.getLoginUrl())
                            .addLogoutHandler((req, res, auth) -> CrustContext.invalidate())
                            .clearAuthentication(true)
                            .invalidateHttpSession(true)
                    );

        }

        // 异常处理器（如果开启了Hydrogen/Uniform模块，则交由Uniform模块处理）
        // 认证用户无权限访问处理
        http.exceptionHandling(configurer -> configurer
                .accessDeniedHandler(failureHandler)
                // 认证异常或匿名用户无权限访问处理
                .authenticationEntryPoint(failureHandler));
        // add others http configure
        additionalConfigure(http, props.isStateless());
        return http.build();
    }

    // 配置Web资源，资源根路径需要配置静态资源映射
    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() {
        // 放开静态资源的限制
        return (web) -> {
            if (props.getAllowStaticUrls() != null) {
                web.ignoring().requestMatchers(props.getAllowStaticUrls().toArray(new String[0]));
            }
        };
    }

    /**
     * Custom http configure.
     * @param http  HttpSecurity
     * @param stateless true for a session type, false for a token type
     */
    protected void additionalConfigure(HttpSecurity http, boolean stateless) { }

    /**
     * 自定义配置数据源提供及<code>PasswordEncoder</code>
     * @param provider  DaoAuthenticationProvider
     * @see DaoAuthenticationProvider#setPasswordEncoder(PasswordEncoder)
     */
    protected void configureProvider(DaoAuthenticationProvider provider) {
        provider.setHideUserNotFoundExceptions(false);
        provider.setUserDetailsService(userDetailsService());
    }

    /**
     * Must implement this method used for {@link DaoAuthenticationProvider}.
     * @return UserDetailsService
     */
    protected abstract UserDetailsService userDetailsService();

    /**
     * Custom response for auth or access failure handler.
     * @param isAuth    true if is an auth type
     * @param request   http request
     * @param response  http response
     * @param exception AuthenticationException (auth type) | AccessDeniedException (access type)
     * @throws IOException  if an input or output exception occurred
     * @since 3.14.0
     */
    protected void doFailure(boolean isAuth, HttpServletRequest request, HttpServletResponse response, RuntimeException exception) throws IOException {
        response.setStatus(isAuth ? HttpStatus.UNAUTHORIZED.value(): HttpStatus.FORBIDDEN.value());
        ResultVO<?> source = UniformResult.error(String.valueOf(response.getStatus()), exception == null ? "" : exception.getMessage());
        UniformHandler.matchStatusToWrite(response, source.toMap());
    }

    @Bean
    protected CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Collections.singletonList("*"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "HEAD", "OPTION"));
        configuration.setAllowedHeaders(Collections.singletonList("*"));
        configuration.addExposedHeader(props.getRefreshTokenName());
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    static class DefaultFailureHandler implements AuthenticationFailureHandler, AccessDeniedHandler, AuthenticationEntryPoint {

        private final CrustConfigurerAdapter configurerAdapter;

        public DefaultFailureHandler(CrustConfigurerAdapter configurerAdapter) {
            this.configurerAdapter = configurerAdapter;
        }

        // 认证失败处理器
        @Override
        public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException, ServletException {
            configurerAdapter.doFailure(true, request, response, authException);
        }

        // 认证异常或匿名用户无权限访问处理器
        @Override
        public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException, ServletException {
            this.onAuthenticationFailure(request, response, authException);
        }

        // 认证用户无权限访问拒绝处理器
        @Override
        public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException accessDeniedException) throws IOException, ServletException {
            configurerAdapter.doFailure(false, request, response, accessDeniedException);
        }
    }
}
