package com.github.yizzuide.milkomeda.hydrogen.interceptor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.web.servlet.WebMvcAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.PathMatcher;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;
import org.springframework.web.util.UrlPathHelper;

/**
 * InterceptorConfig
 *
 * @author yizzuide
 * @since 3.0.0
 * Create at 2020/03/28 00:23
 */
@Slf4j
@Configuration
@EnableConfigurationProperties(InterceptorProperties.class)
@AutoConfigureAfter(WebMvcAutoConfiguration.class)
@ConditionalOnProperty(prefix = "milkomeda.hydrogen.interceptor", name = "enable", havingValue = "true")
public class InterceptorConfig {

    @SuppressWarnings("all")
    @Bean
    @ConditionalOnClass(name = "org.springframework.web.servlet.HandlerInterceptor")
    public InterceptorLoader interceptorHandler(InterceptorProperties interceptorProperties, RequestMappingHandlerMapping requestMappingHandlerMapping) {
        return new WebMvcInterceptorLoader(interceptorProperties, requestMappingHandlerMapping);
    }

    @Autowired
    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    public void config(PathMatcher mvcPathMatcher, UrlPathHelper mvcUrlPathHelper) {
        InterceptorHolder.setMvcPathMatcher(mvcPathMatcher);
        InterceptorHolder.setUrlPathHelper(mvcUrlPathHelper);
    }
}
