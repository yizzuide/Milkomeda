package com.github.yizzuide.milkomeda.ice;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * IceClientConfig
 *
 * @author yizzuide
 * @since 1.15.2
 * @version 2.0.0
 * Create at 2019/11/21 11:21
 */
@Configuration
public class IceClientConfig extends IceBasicConfig {
    @Bean
    @ConditionalOnMissingBean
    public IceContext iceContext() {
        return new IceContext();
    }
}
