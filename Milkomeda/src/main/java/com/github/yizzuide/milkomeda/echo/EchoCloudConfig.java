package com.github.yizzuide.milkomeda.echo;

import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

/**
 * EchoCloudConfig
 *
 * @author yizzuide
 * @since 2.5.4
 * Create at 2020/03/01 17:12
 */
@Configuration
@ConditionalOnClass(LoadBalanced.class)
@AutoConfigureAfter(EchoConfig.class)
public class EchoCloudConfig {
    @LoadBalanced
    @Bean("echoCloudRestTemplate")
    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    public RestTemplate simpleRestTemplate(RestTemplateBuilder builder, ClientHttpRequestFactory factory) {
        RestTemplate restTemplate = builder.build();
        restTemplate.setRequestFactory(factory);
        restTemplate.setErrorHandler(new EchoResponseErrorHandler());
        return restTemplate;
    }
}
