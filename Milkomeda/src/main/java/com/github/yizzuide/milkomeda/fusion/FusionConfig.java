package com.github.yizzuide.milkomeda.fusion;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * FusionConfig
 *
 * @author yizzuide
 * @since 2.0.0
 * Create at 2019/12/13 00:53
 */
@Configuration
public class FusionConfig {
    @Bean
    public FusionAspect fusionAspect() {
        return new FusionAspect();
    }
}
