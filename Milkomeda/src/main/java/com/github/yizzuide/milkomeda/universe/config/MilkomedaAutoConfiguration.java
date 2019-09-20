package com.github.yizzuide.milkomeda.universe.config;

import com.github.yizzuide.milkomeda.comet.Comet;
import com.github.yizzuide.milkomeda.comet.CometAspect;
import com.github.yizzuide.milkomeda.fusion.Fusion;
import com.github.yizzuide.milkomeda.fusion.FusionAspect;
import com.github.yizzuide.milkomeda.particle.IdempotentLimiter;
import com.github.yizzuide.milkomeda.particle.Limit;
import com.github.yizzuide.milkomeda.particle.ParticleAspect;
import com.github.yizzuide.milkomeda.pulsar.Pulsar;
import com.github.yizzuide.milkomeda.universe.context.ApplicationContextHolder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.HashMap;
import java.util.Map;

/**
 * MilkomedaAutoConfiguration
 *
 * @author yizzuide
 * @since 0.2.1
 * @version 1.5.0
 * Create at 2019/04/12 11:29
 */
@Slf4j
@Configuration
public class MilkomedaAutoConfiguration {
    @Bean
    public ApplicationContextHolder applicationContextHolder() {
        return new ApplicationContextHolder();
    }

    @Bean("pulsarTaskExecutor")
    public ThreadPoolTaskExecutor taskExecutor() {
        return new ThreadPoolTaskExecutor();
    }

    @Bean
    @ConditionalOnClass(Pulsar.class)
    public Pulsar pulsar() {
        Pulsar pulsar = new Pulsar();
        pulsar.setTimeoutCallback(() -> {
            Map<String, Object> ret = new HashMap<>();
            ret.put("error_message", "PulsarFlow handle timeout");
            return ResponseEntity.status(500).body(ret);
        });
        pulsar.setErrorCallback((Throwable t) -> {
            Map<String, Object> ret = new HashMap<>();
            ret.put("error_message", t.getMessage());
            return ResponseEntity.status(500).body(ret);
        });
        return pulsar;
    }

    @Bean
    @ConditionalOnClass(Comet.class)
    public CometAspect cometAspect() {
        return new CometAspect();
    }

    @Bean
    @ConditionalOnClass(Limit.class)
    public ParticleAspect particleAspect() {
        return new ParticleAspect();
    }

    @Bean
    public IdempotentLimiter idempotentLimiter() {
        return new IdempotentLimiter();
    }

    @Bean
    @ConditionalOnClass(Fusion.class)
    public FusionAspect fusionAspect() {
        return new FusionAspect();
    }
}
