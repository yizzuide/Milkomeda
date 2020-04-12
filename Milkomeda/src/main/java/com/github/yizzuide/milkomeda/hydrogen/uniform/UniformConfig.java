package com.github.yizzuide.milkomeda.hydrogen.uniform;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter;

/**
 * UniformConfig
 *
 * @author yizzuide
 * @since 3.0.0
 * @version 3.0.1
 * Create at 2020/03/25 22:46
 */
@Configuration
@EnableConfigurationProperties(UniformProperties.class)
@ConditionalOnProperty(prefix = "milkomeda.hydrogen.uniform", name = "enable", havingValue = "true")
public class UniformConfig {
    @Bean
    @ConditionalOnMissingBean(name = "uniformHandler")
    public UniformHandler uniformHandler() {
        return new UniformHandler();
    }

    @Autowired
    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    public void configParamResolve(RequestMappingHandlerAdapter adapter) {
    }
}
