package com.github.yizzuide.milkomeda.crust;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * CrustConfig
 *
 * @author yizzuide
 * @since 1.14.0
 * @version 1.16.4
 * Create at 2019/11/11 14:56
 */
@Configuration
@ConditionalOnClass({AuthenticationManager.class})
@EnableConfigurationProperties(CrustProperties.class)
public class CrustConfig {

    @Bean
    public Crust crust() {
        return new Crust();
    }

    @Autowired
    public void configCrustContext(Crust crust) {
        CrustContext.set(crust);
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = "milkomeda.crust", name = "use-bcrypt", havingValue = "true", matchIfMissing = true)
    public BCryptPasswordEncoder bCryptPasswordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
