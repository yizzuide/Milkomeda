package com.github.yizzuide.milkomeda.universe.config;

import com.github.yizzuide.milkomeda.universe.context.ApplicationContextHolder;
import com.github.yizzuide.milkomeda.universe.context.WebContext;
import com.github.yizzuide.milkomeda.universe.env.Environment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.web.servlet.WebMvcAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.PathMatcher;
import org.springframework.web.util.UrlPathHelper;

/**
 * MilkomedaContextConfig
 *
 * @author yizzuide
 * @since 2.0.0
 * @version 3.1.0
 * Create at 2019/12/13 19:09
 */
@Configuration
@AutoConfigureAfter(WebMvcAutoConfiguration.class)
public class MilkomedaContextConfig {

    @Bean
    public Environment env() {
        return new Environment();
    }

    @Bean
    @ConditionalOnMissingBean
    public ApplicationContextHolder applicationContextHolder(Environment env) {
        ApplicationContextHolder applicationContextHolder = new ApplicationContextHolder();
        ApplicationContextHolder.setEnvironment(env);
        return applicationContextHolder;
    }

    @Autowired
    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    public void config(PathMatcher mvcPathMatcher, UrlPathHelper mvcUrlPathHelper) {
        WebContext.setMvcPathMatcher(mvcPathMatcher);
        WebContext.setUrlPathHelper(mvcUrlPathHelper);
    }
}
