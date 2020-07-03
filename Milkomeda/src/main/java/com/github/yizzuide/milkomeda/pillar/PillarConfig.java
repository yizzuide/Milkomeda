package com.github.yizzuide.milkomeda.pillar;

import org.springframework.context.annotation.Bean;

/**
 * PillarConfig
 *
 * @author yizzuide
 * @since 3.10.0
 * Create at 2020/07/02 16:59
 */
public class PillarConfig {
    @Bean
    public PillarEntryContext pillarContext() {
        return new PillarEntryContext();
    }
}
