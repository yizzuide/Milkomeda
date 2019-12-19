package com.github.yizzuide.milkomeda.light;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Collections;

/**
 * LightConfig
 *
 * @author yizzuide
 * @version 1.17.0
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
        lightCache.setL1MaxCount(props.getL1MaxCount());
        lightCache.setL1DiscardPercent(props.getL1DiscardPercent());
        lightCache.setStrategy(props.getStrategy());
        lightCache.setStrategyClass(props.getStrategyClass());
        lightCache.setOnlyCacheL1(props.isOnlyCacheL1());
        lightCache.setL2Expire(props.getL2Expire());
        return lightCache;
    }

    @Bean
    public FilterRegistrationBean<LightCacheClearFilter> lightCacheClearFilter() {
        FilterRegistrationBean<LightCacheClearFilter> lightCacheClearFilter = new FilterRegistrationBean<>();
        lightCacheClearFilter.setFilter(new LightCacheClearFilter());
        lightCacheClearFilter.setName("lightCacheClearFilter");
        lightCacheClearFilter.setUrlPatterns(Collections.singleton("/*"));
        return lightCacheClearFilter;
    }
}
