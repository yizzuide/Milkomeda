package com.github.yizzuide.milkomeda.hydrogen.uniform;

import com.github.yizzuide.milkomeda.universe.config.MilkomedaContextConfig;
import com.github.yizzuide.milkomeda.universe.context.ApplicationContextHolder;
import com.github.yizzuide.milkomeda.universe.polyfill.SpringMvcPolyfill;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport;

/**
 * UniformConfig
 *
 * @author yizzuide
 * @since 3.0.0
 * @version 3.0.2
 * @see WebMvcConfigurationSupport#handlerExceptionResolver(org.springframework.web.accept.ContentNegotiationManager)
 * #see ExceptionHandlerExceptionResolver#initExceptionHandlerAdviceCache()
 * #see ExceptionHandlerExceptionResolver#getExceptionHandlerMethod(org.springframework.web.method.HandlerMethod, java.lang.Exception)
 * Create at 2020/03/25 22:46
 */
@Import(MilkomedaContextConfig.class)
@EnableConfigurationProperties(UniformProperties.class)
@ConditionalOnProperty(prefix = "milkomeda.hydrogen.uniform", name = "enable", havingValue = "true")
@Configuration
public class UniformConfig {
    // 注入需要需要使用的ApplicationContext（让MilkomedaContextConfig先配置）
    @SuppressWarnings("unused")
    @Autowired
    private ApplicationContextHolder applicationContextHolder;

    @Bean
    public UniformHandler uniformHandler() {
        return new UniformHandler();
    }

    @Autowired
    public void configParamResolve(HandlerExceptionResolver handlerExceptionResolver) {
        SpringMvcPolyfill.addDynamicExceptionAdvice(handlerExceptionResolver, ApplicationContextHolder.get(),  "uniformHandler");
    }
}
