package com.github.yizzuide.milkomeda.crust;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.BeanIds;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.annotation.web.configurers.ExpressionUrlAuthorizationConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;
import org.springframework.security.web.authentication.logout.HttpStatusReturningLogoutSuccessHandler;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.Collections;
import java.util.function.Supplier;

/**
 * CrustConfigurerAdapter
 * Spring Security配置器适配器
 *
 * @author yizzuide
 * @since 1.14.0
 * @version 3.3.0
 * @see org.springframework.security.web.session.SessionManagementFilter
 * @see org.springframework.security.config.annotation.web.configurers.SessionManagementConfigurer
 * Create at 2019/11/11 18:25
 */
public class CrustConfigurerAdapter extends WebSecurityConfigurerAdapter {

    @Autowired
    private CrustProperties props;

    @Autowired(required = false)
    private BCryptPasswordEncoder passwordEncoder;

    @Override
    public void configure(AuthenticationManagerBuilder auth) {
        CrustAuthenticationProvider authenticationProvider = new CrustAuthenticationProvider(props, passwordEncoder);
        configureProvider(authenticationProvider);
        // 添加自定义身份验证组件
        auth.authenticationProvider(authenticationProvider);
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.csrf().disable()
            .sessionManagement().sessionCreationPolicy(props.isStateless() ?
                SessionCreationPolicy.STATELESS : SessionCreationPolicy.IF_REQUIRED).and()
            .formLogin().disable()
            // 支持跨域，从CorsConfigurationSource中取跨域配置
            .cors().and();

        // 配置预设置
        presetConfigure(http);

        // 如果是无状态方式
        if (props.isStateless()) {
            // 应用Token认证配置器，忽略登出请求
            http.apply(new CrustAuthenticationConfigurer<>(authFailureHandler())).permissiveRequestUrls(props.getLogoutUrl())
                    .and()
                    .logout()
                    .logoutUrl(props.getLogoutUrl())
                    .addLogoutHandler((req, res, auth) -> CrustContext.invalidate())
                    .logoutSuccessHandler(new HttpStatusReturningLogoutSuccessHandler());
        } else {
            // 自定义session方式登录
            http.httpBasic().authenticationEntryPoint(new LoginUrlAuthenticationEntryPoint(props.getLoginUrl()))
                    .and()
                    .sessionManagement()
                    .sessionFixation().changeSessionId()
                    .sessionAuthenticationErrorUrl(props.getLoginUrl())
                    .sessionAuthenticationFailureHandler(authFailureHandler().get()).and()
            .logout()
                    .logoutUrl(props.getLogoutUrl())
                    .addLogoutHandler((req, res, auth) -> CrustContext.invalidate())
                    .logoutSuccessUrl(props.getLoginUrl())
                    .invalidateHttpSession(true);
        }
    }

    /**
     * 自定义配置数据源提供及<code>PasswordEncoder</code>
     * @param provider  DaoAuthenticationProvider
     */
    protected void configureProvider(DaoAuthenticationProvider provider) { }

    /**
     * 认证失败处理器
     *
     * @return Supplier
     */
    @NonNull
    protected Supplier<AuthenticationFailureHandler> authFailureHandler() {
        return () -> (request, response, exception) -> response.setStatus(HttpStatus.UNAUTHORIZED.value());
    }

    /**
     * 预设置添加允许访问路径
     *
     * @param http HttpSecurity
     * @throws Exception 配置异常
     */
    protected void presetConfigure(HttpSecurity http) throws Exception {
        ExpressionUrlAuthorizationConfigurer<HttpSecurity>.ExpressionInterceptUrlRegistry urlRegistry =
                http.authorizeRequests()
                        // 跨域预检请求
                        .antMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        // 登录
                        .antMatchers(props.getLoginUrl()).permitAll()
                        // web jars、druid
                        .antMatchers("/webjars/**").permitAll()
                        .antMatchers("/druid/**").permitAll()
                        // swagger
                        .antMatchers("/swagger-ui.html").permitAll()
                        .antMatchers("/swagger-resources").permitAll()
                        .antMatchers("/webjars/springfox-swagger-ui/**").permitAll()
                        // 服务监控
                        .antMatchers("/actuator/**").permitAll();
        // 自定义额外允许路径
        additionalConfigure(urlRegistry, http);
        // 其他所有请求需要身份认证
        urlRegistry.anyRequest().authenticated();
    }

    /**
     * 自定义添加允许匿名访问的路径
     *
     * @param urlRegistry URL配置对象
     * @param http        HttpSecurity
     * @throws Exception 配置异常
     */
    protected void additionalConfigure(ExpressionUrlAuthorizationConfigurer<HttpSecurity>.
                                               ExpressionInterceptUrlRegistry urlRegistry, HttpSecurity http) throws Exception {
    }

    @Bean(name = BeanIds.AUTHENTICATION_MANAGER)
    @Override
    public AuthenticationManager authenticationManager() throws Exception {
        return super.authenticationManager();
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
}
