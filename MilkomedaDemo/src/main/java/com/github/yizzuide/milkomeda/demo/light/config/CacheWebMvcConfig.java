package com.github.yizzuide.milkomeda.demo.light.config;

import com.github.yizzuide.milkomeda.fusion.FusionAspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * WebMvcConfig
 *
 * @author yizzuide
 * Create at 2019/07/02 11:00
 */
@Configuration
public class CacheWebMvcConfig implements WebMvcConfigurer {

    @Autowired
    public void configFusion(FusionAspect fusionAspect) {
        fusionAspect.setConverter((tag, retObj) -> retObj);
    }
}
