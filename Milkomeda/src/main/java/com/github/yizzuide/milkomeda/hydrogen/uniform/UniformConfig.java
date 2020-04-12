package com.github.yizzuide.milkomeda.hydrogen.uniform;

import com.github.yizzuide.milkomeda.universe.polyfill.SpringMvcPolyfill;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport;

/**
 * UniformConfig
 *
 * @author yizzuide
 * @since 3.0.0
 * @version 3.0.1
 * @see WebMvcConfigurationSupport#handlerExceptionResolver(org.springframework.web.accept.ContentNegotiationManager)
 * #see ExceptionHandlerExceptionResolver#initExceptionHandlerAdviceCache()
 * #see ExceptionHandlerExceptionResolver#getExceptionHandlerMethod(org.springframework.web.method.HandlerMethod, java.lang.Exception)
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

    @Autowired
    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    public void configParamResolve(HandlerExceptionResolver handlerExceptionResolver) {
        SpringMvcPolyfill.addDynamicExceptionAdvice(handlerExceptionResolver, "uniformHandler");
    }
}
