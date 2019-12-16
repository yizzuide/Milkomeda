package com.github.yizzuide.milkomeda.universe.config;

import com.github.yizzuide.milkomeda.comet.CometAspect;
import com.github.yizzuide.milkomeda.fusion.FusionAspect;
import com.github.yizzuide.milkomeda.particle.ParticleConfig;
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
 * @version 1.18.2
 * Create at 2019/04/12 11:29
 */
@Slf4j
@Configuration
@Import({ParticleConfig.class, MilkomedaContextConfig.class})
@EnableConfigurationProperties(MilkomedaProperties.class)
public class MilkomedaAutoConfiguration {
    @Bean
    public CometAspect cometAspect() {
        return new CometAspect();
    }

    @Bean
    public FusionAspect fusionAspect() {
        return new FusionAspect();
    }
}
