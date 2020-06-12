package com.github.yizzuide.milkomeda.halo;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * HaloConfig
 *
 * @author yizzuide
 * @since 2.5.0
 * @version 3.6.0
 * Create at 2020/01/30 18:43
 */
@Configuration
@EnableConfigurationProperties(HaloProperties.class)
public class HaloConfig {
    @Bean
    public HaloContext haloContext() {
        return new HaloContext();
    }

    @Bean
    public HaloInterceptor haloInterceptor() {
        return new HaloInterceptor();
    }
}
