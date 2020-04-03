package com.github.yizzuide.milkomeda.hydrogen.filter;

import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * TomcatFilterConfig
 * Tomcat容器配置
 *
 * @author yizzuide
 * @since 3.0.0
 * Create at 2020/04/03 00:51
 */
@ConditionalOnClass(name = {"org.apache.catalina.core.StandardContext"})
@Configuration
public class TomcatFilterConfig {
    @Bean
    public FilterLoader filterLoader() {
        return new TomcatFilterLoader();
    }
}
