package com.github.yizzuide.milkomeda.halo;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;

/**
 * HaloConfig
 *
 * @author yizzuide
 * @since 2.5.0
 * @version 3.5.2
 * Create at 2020/01/30 18:43
 */
@Configuration
@EnableConfigurationProperties(HaloProperties.class)
public class HaloConfig {
    @Bean
    @Order(Ordered.HIGHEST_PRECEDENCE)
    public HaloContext haloContext() {
        return new HaloContext();
    }

    @Bean
    public HaloInterceptor haloInterceptor() {
        return new HaloInterceptor();
    }
}
