package com.github.yizzuide.milkomeda.ice;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * IceConfig
 *
 * @author yizzuide
 * @since 1.15.0
 * @version 2.0.0
 * Create at 2019/11/16 17:19
 */
@Configuration
public class IceConfig extends IceBasicConfig {
    @Bean
    @ConditionalOnProperty(prefix = "milkomeda.ice", name = "enable-task", havingValue = "true")
    public IceContext iceContext() {
        return new IceContext();
    }

    @Bean
    @ConditionalOnProperty(prefix = "milkomeda.ice", name = "enable-job-timer", havingValue = "true", matchIfMissing = true)
    public DelayTimer delayTimer() {
        return new DelayTimer();
    }
}
