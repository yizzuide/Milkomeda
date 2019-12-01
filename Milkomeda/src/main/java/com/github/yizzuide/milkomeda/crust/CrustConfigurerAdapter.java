package com.github.yizzuide.milkomeda.crust;

import com.github.yizzuide.milkomeda.universe.context.ApplicationContextHolder;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.BeanIds;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.annotation.web.configurers.ExpressionUrlAuthorizationConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.logout.HttpStatusReturningLogoutSuccessHandler;
import org.springframework.security.web.header.Header;
import org.springframework.security.web.header.writers.StaticHeadersWriter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.function.Supplier;

/**
 * CrustConfigurerAdapter
 * Spring Security配置器适配器
 *
 * @author yizzuide
 * @since 1.14.0
 * @version 1.16.1
 * Create at 2019/11/11 18:25
 */
public class CrustConfigurerAdapter extends WebSecurityConfigurerAdapter {

    @Override
    public void configure(AuthenticationManagerBuilder auth) {
        // 添加自定义身份验证组件
        CrustUserDetailsService userDetailsService = ApplicationContextHolder.get().getBean(CrustUserDetailsService.class);
        CrustAuthenticationProvider authenticationProvider = new CrustAuthenticationProvider(userDetailsService);
        configureProvider(authenticationProvider);
        auth.authenticationProvider(authenticationProvider);
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        CrustProperties props = ApplicationContextHolder.get().getBean(CrustProperties.class);
        http.csrf().disable()
            .sessionManagement().sessionCreationPolicy(props.isStateless() ?
                SessionCreationPolicy.STATELESS : SessionCreationPolicy.IF_REQUIRED).and()
            .formLogin().disable()
            // 支持跨域，从CorsConfigurationSource中取跨域配置
            .cors().and()
            // 添加header设置，支持跨域和ajax请求
            .headers().addHeaderWriter(new StaticHeadersWriter(Arrays.asList(
                // 支持所有源的访问
                new Header("Access-control-Allow-Origin", "*"),
                // 使ajax请求能够取到header中的jwt token信息
                new Header("Access-Control-Expose-Headers", "Authorization"))))
            .and()
            // 拦截OPTIONS请求，直接返回header
            .addFilterAfter(new OptionsRequestFilter(), CorsFilter.class);

        // 如果是无状态方式
        if (props.isStateless()) {
            // 应用Token认证配置器，忽略登出请求
            http.apply(new CrustAuthenticationConfigurer<>(authFailureHandler())).permissiveRequestUrls(props.getLogoutUrl());
        }

        http.logout()
                .logoutUrl(props.getLogoutUrl())
                .addLogoutHandler((req, res, auth) -> SecurityContextHolder.clearContext())
                .logoutSuccessHandler(new HttpStatusReturningLogoutSuccessHandler());

        presetConfigure(http, props);
    }

    /**
     * 自定义配置数据源提供及<code>PasswordEncoder</code>
     * @param provider  DaoAuthenticationProvider
     */
    protected void configureProvider(DaoAuthenticationProvider provider) { }

    /**
     * 认证失败处理器
     */
    protected Supplier<AuthenticationFailureHandler> authFailureHandler() {
        return () -> (request, response, exception) -> response.setStatus(HttpStatus.UNAUTHORIZED.value());
    }

    /**
     * 预设置添加允许访问路径
     *
     * @param http HttpSecurity
     * @param props 配置
     * @throws Exception 配置异常
     */
    protected void presetConfigure(HttpSecurity http, CrustProperties props) throws Exception {
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
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "HEAD", "OPTION"));
        configuration.setAllowedHeaders(Collections.singletonList("*"));
        configuration.addExposedHeader("Authorization");
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}

class OptionsRequestFilter extends OncePerRequestFilter {
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        if (request.getMethod().equals("OPTIONS")) {
            response.setHeader("Access-Control-Allow-Methods", "GET,POST,OPTIONS,HEAD");
            response.setHeader("Access-Control-Allow-Headers", response.getHeader("Access-Control-Request-Headers"));
            return;
        }
        filterChain.doFilter(request, response);
    }

}
