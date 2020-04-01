package com.github.yizzuide.milkomeda.hydrogen.filter;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * HydrogenFilterConfig
 *
 * @author yizzuide
 * @since 3.0.0
 * Create at 2020/04/01 18:18
 */
@Configuration
@ConditionalOnProperty(prefix = "milkomeda.hydrogen.filter", name = "enable", havingValue = "true")
public class FilterConfig {
    @Bean
    public FilterLoader hydrogenFilter() {
        return new FilterLoader();
    }

    @Bean
    public ServletContextListener servletContextListener() {
       return new ServletContextListener();
    }
}
