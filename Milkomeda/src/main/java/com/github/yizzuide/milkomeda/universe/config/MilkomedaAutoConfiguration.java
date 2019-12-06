package com.github.yizzuide.milkomeda.universe.config;

import com.github.yizzuide.milkomeda.comet.CometAspect;
import com.github.yizzuide.milkomeda.fusion.FusionAspect;
import com.github.yizzuide.milkomeda.particle.ParticleConfig;
import com.github.yizzuide.milkomeda.universe.context.ApplicationContextHolder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * MilkomedaAutoConfiguration
 *
 * @author yizzuide
 * @since 0.2.1
 * @version 1.16.0
 * Create at 2019/04/12 11:29
 */
@Slf4j
@Configuration
@Import({ParticleConfig.class})
@EnableConfigurationProperties(MilkomedaProperties.class)
public class MilkomedaAutoConfiguration {
    @Bean
    public ApplicationContextHolder applicationContextHolder() {
        return new ApplicationContextHolder();
    }

    @Bean
    public CometAspect cometAspect() {
        return new CometAspect();
    }

    @Bean
    public FusionAspect fusionAspect() {
        return new FusionAspect();
    }
}
