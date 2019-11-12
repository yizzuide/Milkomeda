package com.github.yizzuide.milkomeda.crust;

import com.github.yizzuide.milkomeda.universe.context.ApplicationContextHolder;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.BeanIds;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.annotation.web.configurers.ExpressionUrlAuthorizationConfigurer;
import org.springframework.security.core.context.SecurityContextHolder;
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

/**
 * CrustConfigurerAdapter
 * Spring Security配置器适配器
 * <br>
 * <h3>继承这个类，添加上以下注解：</h3>
 * <blockquote>
 * <pre class="code">
 * <code>@EnableWebSecurity</code>
 * <code>@EnableGlobalMethodSecurity(prePostEnabled = true)</code>
 * </pre>
 * </blockquote>
 *
 * @author yizzuide
 * @since 1.14.0
 * Create at 2019/11/11 18:25
 */
public class CrustConfigurerAdapter extends WebSecurityConfigurerAdapter {

    @Override
    public void configure(AuthenticationManagerBuilder auth) {
        // 添加自定义身份验证组件
        auth.authenticationProvider(new CrustAuthenticationProvider(ApplicationContextHolder.get().getBean(CrustUserDetailsService.class)));
    }

    @Override
    protected final void configure(HttpSecurity http) throws Exception {
        http.csrf().disable()
            .sessionManagement().disable()
            .formLogin().disable()
            // 支持跨域，从CorsConfigurationSource中取跨域配置
            .cors()
            .and()
            // 添加header设置，支持跨域和ajax请求
            .headers().addHeaderWriter(new StaticHeadersWriter(Arrays.asList(
                // 支持所有源的访问
                new Header("Access-control-Allow-Origin", "*"),
                // 使ajax请求能够取到header中的jwt token信息
                new Header("Access-Control-Expose-Headers", "Authorization"))))
            .and()
            // 拦截OPTIONS请求，直接返回header
            .addFilterAfter(new OptionsRequestFilter(), CorsFilter.class)
            // 应用认证配置器，忽略登出请求
            .apply(new CrustAuthenticationConfigurer<>()).permissiveRequestUrls("/logout")
            .and()
            .logout()
                .addLogoutHandler((req, res, auth) -> SecurityContextHolder.clearContext())
                .logoutSuccessHandler(new HttpStatusReturningLogoutSuccessHandler());
        presetConfigure(http);
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
                        .antMatchers("/login").permitAll()
                        // web jars、druid
                        .antMatchers("/webjars/**").permitAll()
                        .antMatchers("/druid/**").permitAll()
                        // swagger
                        .antMatchers("/swagger-ui.html").permitAll()
                        .antMatchers("/swagger-resources").permitAll()
                        .antMatchers("/v2/api-docs").permitAll()
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
