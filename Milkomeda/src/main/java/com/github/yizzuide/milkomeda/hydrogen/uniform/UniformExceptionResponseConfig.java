package com.github.yizzuide.milkomeda.hydrogen.uniform;

import com.github.yizzuide.milkomeda.hydrogen.core.HydrogenProperties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * UniformExceptionResponseConfig
 *
 * @author yizzuide
 * @since 3.0.0
 * Create at 2020/03/25 22:46
 */
@Configuration
@EnableConfigurationProperties(HydrogenProperties.class)
@ConditionalOnProperty(prefix = "milkomeda.hydrogen.uniform", name = "enable", havingValue = "true")
public class UniformExceptionResponseConfig {
    @Bean
    public UniformExceptionResponseHandler responseFrontExceptionHandler() {
        return new UniformExceptionResponseHandler();
    }
}
