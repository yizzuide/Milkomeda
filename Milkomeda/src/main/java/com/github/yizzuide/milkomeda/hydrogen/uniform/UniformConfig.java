package com.github.yizzuide.milkomeda.hydrogen.uniform;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * UniformConfig
 *
 * @author yizzuide
 * @since 3.0.0
 * Create at 2020/03/25 22:46
 */
@Configuration
@EnableConfigurationProperties(UniformProperties.class)
@ConditionalOnProperty(prefix = "milkomeda.hydrogen.uniform", name = "enable", havingValue = "true")
public class UniformConfig {
    @Bean
    public UniformHandler uniformHandler() {
        return new UniformHandler();
    }
}
