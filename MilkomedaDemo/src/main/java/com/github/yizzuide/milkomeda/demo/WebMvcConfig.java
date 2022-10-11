package com.github.yizzuide.milkomeda.demo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.endpoint.SanitizingFunction;
import org.springframework.boot.autoconfigure.web.WebProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.PropertySource;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import static org.springframework.boot.actuate.endpoint.SanitizableData.SANITIZED_VALUE;

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

// Springboot 2.3不支持@ActiveProfiles({"dev,test"})，它将识别为单个字符串：dev,test
@ActiveProfiles({"dev","test"})
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {
    // Springboot 2.6: Injecting `Resources` directly no longer works as this configuration has been harmonized in WebProperties
    @Autowired
    private WebProperties webProperties;

    // Springboot 2.6: Spring Boot现在可以清理 /env 和 /configprops 端点中存在的敏感值
    @Bean
    public SanitizingFunction mysqlSanitizingFunction() {
        return data -> {
            PropertySource<?> propertySource = data.getPropertySource();
            if (propertySource.getName().contains("develop.properties")) {
                if (data.getKey().equals("mysql.user")) {
                    return data.withValue(SANITIZED_VALUE);
                }
            }
            return data;
        };
    }
}
