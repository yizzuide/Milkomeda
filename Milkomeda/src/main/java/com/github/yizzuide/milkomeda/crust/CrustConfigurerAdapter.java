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
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.logout.HttpStatusReturningLogoutSuccessHandler;

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
        presetConfigure(http);

        // token验证过滤器
        http.addFilterBefore(new CrustAuthenticationFilter(authenticationManager()), UsernamePasswordAuthenticationFilter.class);
    }

    @Bean(name = BeanIds.AUTHENTICATION_MANAGER)
    @Override
    public AuthenticationManager authenticationManager() throws Exception {
        return super.authenticationManager();
    }

    /**
     * 预设置添加允许访问路径
     * @param http  HttpSecurity
     * @throws Exception 配置异常
     */
    protected void presetConfigure(HttpSecurity http) throws Exception {
        ExpressionUrlAuthorizationConfigurer<HttpSecurity>.ExpressionInterceptUrlRegistry urlRegistry =
                // 禁用 csrf, 由于使用的是JWT就不需要csrf
                http.cors().and().csrf().disable()
                .authorizeRequests()
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

        // 退出登录处理器
        http.logout().logoutSuccessHandler(new HttpStatusReturningLogoutSuccessHandler());
    }

    /**
     * 自定义添加允许匿名访问的路径
     * @param urlRegistry   URL配置对象
     * @param http  HttpSecurity
     * @throws Exception 配置异常
     */
    protected void additionalConfigure(ExpressionUrlAuthorizationConfigurer<HttpSecurity>.
                                               ExpressionInterceptUrlRegistry urlRegistry, HttpSecurity http) throws Exception { }
}
