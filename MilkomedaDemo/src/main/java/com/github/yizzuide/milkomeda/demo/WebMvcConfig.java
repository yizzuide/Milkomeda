package com.github.yizzuide.milkomeda.demo;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * WebMvcConfig
 *
 * @author yizzuide
 * Create at 2020/04/03 18:33
 */
// 这个注解会自动配置：DelegatingWebMvcConfiguration -> WebMvcConfigurationSupport
//@EnableWebMvc
// 关闭会自动配置：WebMvcAutoConfiguration（项目中不存在WebMvcConfigurationSupport Bean时配置，也就是没加@EnableWebMvc & WebMvcAutoConfigurationAdapter（这个会把ContentNegotiation配置上，包括忽略请求后缀数据匹配）
//                          --导入--> EnableWebMvcConfiguration（相当于@EnableWebMvc，继承自DelegatingWebMvcConfiguration） -> WebMvcConfigurationSupport
// 总结：不添加@EnableWebMvc，会配置的更多更全面，拥有添加@EnableWebMvc的所有配置
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {
    // spring.resources.add-mappings=false，默认的静态资源映射被禁用后，需要手动开启
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 设置静态资源，用于Spring Security配置
        registry.addResourceHandler("/**").addResourceLocations("classpath:/static/");
    }
}
