package com.github.yizzuide.milkomeda.wormhole;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * WormholeConfig
 *
 * @author yizzuide
 * @since 3.3.0
 * Create at 2020/05/05 13:58
 */
@Configuration
public class WormholeConfig {
    @Bean
    public WormholeRegistration domainRegistration() {
        return new WormholeRegistration();
    }

    @Bean
    public WormholeEventBus wormholeEventBus() {
        WormholeEventBus wormholeEventBus = new WormholeEventBus();
        WormholeHolder.setEventBus(wormholeEventBus);
        return wormholeEventBus;
    }
}
