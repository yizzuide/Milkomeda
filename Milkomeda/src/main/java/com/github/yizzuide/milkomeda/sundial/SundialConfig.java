package com.github.yizzuide.milkomeda.sundial;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @date: 2020/5/8
 * @author: jsq
 * @email: 786063250@qq.com
 * @describe:
 */
@Configuration
@EnableConfigurationProperties(SundialProperties.class)
public class SundialConfig {


    @Bean
    public DataSourceFactory dataSourceFactory(){
        return new DataSourceFactory();
    }

    @Bean
    public DataSourcePropertiesBindingPostProcessor dataSourcePropertiesBindingPostProcessor() {
        return new DataSourcePropertiesBindingPostProcessor();
    }
}
