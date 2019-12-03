package com.github.yizzuide.milkomeda.demo.light.config;

import com.github.yizzuide.milkomeda.demo.light.handler.RequestInterceptor;
import com.github.yizzuide.milkomeda.fusion.FusionAspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import javax.annotation.Resource;

/**
 * WebMvcConfig
 *
 * @author yizzuide
 * Create at 2019/07/02 11:00
 */
@Configuration
public class CacheWebMvcConfig implements WebMvcConfigurer {

    @Resource
    private RequestInterceptor requestInterceptor;

    @Autowired
    public void configFusion(FusionAspect fusionAspect) {
        fusionAspect.setConverter((tag, retObj) -> retObj);
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(requestInterceptor).order(-1).addPathPatterns("/order/**");
    }
}
