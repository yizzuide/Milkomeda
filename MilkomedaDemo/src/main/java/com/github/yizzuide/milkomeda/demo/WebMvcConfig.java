package com.github.yizzuide.milkomeda.demo;

import com.fasterxml.jackson.core.StreamReadConstraints;
import lombok.extern.slf4j.Slf4j;
import org.apache.coyote.ProtocolHandler;
import org.apache.coyote.http11.AbstractHttp11Protocol;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.endpoint.SanitizingFunction;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.boot.autoconfigure.web.WebProperties;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.context.SmartLifecycle;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.PropertySource;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * WebMvcConfig
 *
 * @author yizzuide
 * <br>
 * Create at 2020/04/03 18:33
 */
// 这个注解会自动配置：DelegatingWebMvcConfiguration -> WebMvcConfigurationSupport
//@EnableWebMvc
// 不使用@EnableWebMvc, Spring boot 2.0时自动配置WebMvcAutoConfiguration, 启用条件：
//      1. 项目中不存在WebMvcConfigurationSupport Bean，也就是没加@EnableWebMvc
//      2. 存在WebMvcConfigurer类
// 自动配置一个WebMvcConfigurer内部bean：WebMvcAutoConfigurationAdapter
//    这个会把ContentNegotiation配置上，包括忽略请求后缀数据匹配，并导入
//    EnableWebMvcConfiguration（等同于@EnableWebMvc，继承自DelegatingWebMvcConfiguration -> WebMvcConfigurationSupport
// 总结：在Spring Boot 2.0不添加@EnableWebMvc，会配置的更多更全面，拥有添加@EnableWebMvc的所有配置

// Spring Boot 2.3: 不支持@ActiveProfiles({"dev,test"})，它将识别为单个字符串：dev,test
@ActiveProfiles({"dev","test"})
@Configuration
@Slf4j
public class WebMvcConfig implements WebMvcConfigurer, SmartLifecycle {
    // Spring Boot 2.6: Injecting `Resources` directly no longer works as this configuration has been harmonized in WebProperties
    @Autowired
    private WebProperties webProperties; // webProperties.getResources();

    // Spring Boot 2.6: 现在可以清理 /env 和 /configprops 端点中存在的敏感值
    // Spring Boot 2.7: SanitizingFunction现已支持Ordered排序，在SanitizableData一旦赋值后中止其它调用
    @Order(Integer.MIN_VALUE)
    @Bean
    public SanitizingFunction mysqlSanitizingFunction() {
        return data -> {
            PropertySource<?> propertySource = data.getPropertySource();
            if (propertySource.getName().contains("develop.properties")) {
                if (data.getKey().equals("mysql.user")) {
                    // Spring Boot 3.1: A withSanitizedValue utility method has been added to SanitizableData
                    return data.withSanitizedValue();
                }
            }
            return data;
        };
    }

    // Spring Boot 3.0: The trailing slash matching configuration option has been deprecated, its default value set to false.
    //  Such as "/some/greeting" not match "/some/greeting/"
    /*@Override
    public void configurePathMatch(PathMatchConfigurer configurer) {
        configurer.setUseTrailingSlashMatch(false);
    }*/

    // Spring Boot 3.0: To limit the max header size of an HTTP response on Tomcat
    //  or Jetty (the only two servers that support such a setting), use a WebServerFactoryCustomizer.
    @Bean
    public WebServerFactoryCustomizer<TomcatServletWebServerFactory> webServerFactoryCustomizer() {
        return factory -> factory.addConnectorCustomizers(connector -> {
            ProtocolHandler handler = connector.getProtocolHandler();
            if (handler instanceof AbstractHttp11Protocol<?> http11Protocol) {
                http11Protocol.setMaxHttpResponseHeaderSize(-1);
            }
        });
    }

    // Spring Boot 3.0: In the absence of Reactor Netty, Jetty’s reactive client, and the Apache HTTP client a JdkClientHttpConnector
    //  will now be Auto-configured. This allows WebClient to be used with the JDK’s HttpClient.

    // Spring Boot 3.1: Spring Kafka ContainerCustomizer beans are now applied to the Auto-configured KafkaListenerContainerFactory.
    //  BatchInterceptor beans are now applied to the Auto-configured ConcurrentKafkaListenerContainerFactory.

    // Spring Boot 3.1: RabbitTemplateCustomizer has been introduced. Beans of this type will customize the Auto-configured RabbitTemplate.


    // Spring Boot 3.0: FlywayConfigurationCustomizer beans are now called to customize the FluentConfiguration after
    //  any Callback and JavaMigration beans have been added to the configuration. An application that defines
    //  Callback and JavaMigration beans and adds callbacks and Java migrations using a customizer may have
    //  to be updated to ensure that the intended callbacks and Java migrations are used.
    /*@Bean
    public FlywayConfigurationCustomizer flywayConfigurationCustomizer() {
        return configuration -> {};
    }*/

    // Spring Boot 3.0: Auto-configuration for the new Elasticsearch Java Client has been introduced.
    //  It can be configured using the existing spring.elasticsearch.* configuration properties.
    // The Auto-configuration for the new client does not use the Auto-configuration ObjectMapper for JSON mapping.
    //  This is to prevent clashes between the needs of the application and the needs of Elasticsearch.
    //  To control the ObjectMapper that is used, define a JacksonJsonpMapper bean.
    /*@Bean
    public JacksonJsonpMapper jacksonJsonpMapper(ObjectMapper objectMapper) { // using the context’s ObjectMapper
        return new JacksonJsonpMapper(objectMapper);
    }*/

    // Spring Boot 3.1: One notable change in Jackson 2.15 is the introduction of processing limits.
    @Bean
    public Jackson2ObjectMapperBuilderCustomizer customStreamReadConstraints() {
        return (builder) -> builder.postConfigurer((objectMapper) -> objectMapper.getFactory()
                .setStreamReadConstraints(StreamReadConstraints.builder().maxNestingDepth(2000).build()));
    }

    @Override
    public void start() {
        log.info("Application started!");
    }

    @Override
    public boolean isRunning() {
        return true;
    }

    // Spring Boot 3.0: The phases used by the SmartLifecycle implementations for graceful shutdown have been updated.
    //  Graceful shutdown now begins in phase `SmartLifecycle.DEFAULT_PHASE - 2048` and the web server is stopped in phase `SmartLifecycle.DEFAULT_PHASE - 1024`.
    //  Any SmartLifecycle implementations that were participating in graceful shutdown should be updated accordingly.
    @Override
    public void stop() {
        log.info("Application has to stop!");
    }

    @Override
    public int getPhase() {
        return SmartLifecycle.DEFAULT_PHASE - 2000;
    }
}
