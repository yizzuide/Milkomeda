package com.github.yizzuide.milkomeda.hydrogen;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * UniformResponseExceptionConfig
 *
 * @author yizzuide
 * @since 2.8.0
 * Create at 2020/03/25 22:46
 */
@Configuration
@EnableConfigurationProperties(HydrogenProperties.class)
@ConditionalOnProperty(prefix = "milkomeda.hydrogen.response-front-exception", name = "enable", havingValue = "true")
public class UniformResponseExceptionConfig {
    @Bean
    public UniformResponseExceptionHandler responseFrontExceptionHandler() {
        return new UniformResponseExceptionHandler();
    }
}
