package com.github.yizzuide.milkomeda.light;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * LightConfig
 *
 * @author yizzuide
 * @since 1.17.0
 * @version 3.3.0
 * Create at 2019/12/03 16:22
 */
@Configuration
@EnableConfigurationProperties(LightProperties.class)
public class LightConfig {

    @Autowired
    private LightProperties props;

    @Bean
    @ConditionalOnMissingBean
    public LightCacheAspect lightCacheAspect() {
        return new LightCacheAspect();
    }

    @Bean(LightCacheAspect.DEFAULT_BEAN_NAME)
    public Cache lightCache() {
        LightCache lightCache = new LightCache();
        lightCache.configFrom(props);
        return lightCache;
    }

    @Bean
    public LightCacheCleanAstrolabeHandler lightCacheCleanAstrolabeHandler() {
        return new LightCacheCleanAstrolabeHandler();
    }
}
