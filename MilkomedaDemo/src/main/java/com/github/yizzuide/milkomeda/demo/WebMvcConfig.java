package com.github.yizzuide.milkomeda.demo;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * WebMvcConfig
 *
 * @author yizzuide
 * Create at 2020/04/03 18:33
 */
// 这个注解会自动配置：DelegatingWebMvcConfiguration -> WebMvcConfigurationSupport
// 关闭会自动配置：WebMvcAutoConfiguration（不存在WebMvcConfigurationSupport Bean时配置，也就是没加@EnableWebMvc & WebMvcAutoConfigurationAdapter（这个会把ContentNegotiation配置上，包括忽略请求后缀数据匹配） -> EnableWebMvcConfiguration（相关于@EnableWebMvc） -> WebMvcConfigurationSupport
//@EnableWebMvc
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {
}
