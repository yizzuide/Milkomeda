package com.github.yizzuide.milkomeda.demo.crust.config;

import com.github.yizzuide.milkomeda.crust.CrustConfigurerAdapter;
import com.github.yizzuide.milkomeda.demo.crust.provider.UserDetailsService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

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
}
