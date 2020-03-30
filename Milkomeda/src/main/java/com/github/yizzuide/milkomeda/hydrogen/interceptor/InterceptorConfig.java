package com.github.yizzuide.milkomeda.hydrogen.interceptor;

import com.github.yizzuide.milkomeda.hydrogen.core.HydrogenProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.web.servlet.WebMvcAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

/**
 * InterceptorConfig
 *
 * @author yizzuide
 * @since 3.0.0
 * Create at 2020/03/28 00:23
 */
@Slf4j
@Configuration
@EnableConfigurationProperties(HydrogenProperties.class)
@AutoConfigureAfter(WebMvcAutoConfiguration.class)
public class InterceptorConfig {

    @SuppressWarnings("all")
    @Bean
    public InterceptorHandler interceptorHandler(RequestMappingHandlerMapping requestMappingHandlerMapping) {
        return new InterceptorHandler(requestMappingHandlerMapping);
    }
}
