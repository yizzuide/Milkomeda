package com.github.yizzuide.milkomeda.demo.crust.config;

import com.github.yizzuide.milkomeda.crust.CrustConfigurerAdapter;
import com.github.yizzuide.milkomeda.demo.crust.provider.UserDetailsService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.ExpressionUrlAuthorizationConfigurer;

/**
 * WebSecurityConfig
 *
 * @author yizzuide
 * Create at 2019/11/11 23:35
 */
@Configuration
public class WebSecurityConfig extends CrustConfigurerAdapter {

    @Bean
    public UserDetailsService userDetailsService() {
        return new UserDetailsService();
    }

    @Override
    protected void configureProvider(DaoAuthenticationProvider provider) {
        provider.setUserDetailsService(userDetailsService());
    }

    @Override
    protected void additionalConfigure(ExpressionUrlAuthorizationConfigurer<HttpSecurity>.ExpressionInterceptUrlRegistry urlRegistry, HttpSecurity http) throws Exception {
        // 允许其它测试模块访问
        urlRegistry
                .antMatchers("/collect/**").permitAll()
                .antMatchers("/echo/**").permitAll()
                .antMatchers("/test/**").permitAll()
                .antMatchers("/order/**").permitAll()
                .antMatchers("/particle/**").permitAll()
                .antMatchers("/pay/**").permitAll()
                .antMatchers("/user/**").permitAll()
                .antMatchers("/ice/**").permitAll()
                .antMatchers("/job/**").permitAll()
                .antMatchers("/neutron/**").permitAll();
    }
}
