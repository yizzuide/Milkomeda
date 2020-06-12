package com.github.yizzuide.milkomeda.ice;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * IceServerConfig
 *
 * @author yizzuide
 * @since 1.15.2
 * @version 3.7.2
 * Create at 2019/11/21 11:14
 */
@Configuration
public class IceServerConfig extends IceBasicConfig {

    @Bean
    public DelegatingDelayJobHandler delegatingDelayJobHandler() {
        return new DelegatingDelayJobHandler();
    }

    @Bean
    @ConditionalOnMissingBean
    public DelayTimer delayTimer() {
        return new DelayTimer();
    }
}
