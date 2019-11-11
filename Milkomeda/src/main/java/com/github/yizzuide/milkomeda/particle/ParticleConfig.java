package com.github.yizzuide.milkomeda.particle;

import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * ParticleConfig
 *
 * @author yizzuide
 * @since 1.14.0
 * Create at 2019/11/11 11:26
 */
@Configuration
@AutoConfigureAfter(RedisAutoConfiguration.class)
public class ParticleConfig {
    @Bean
    public ParticleAspect particleAspect() {
        return new ParticleAspect();
    }

    @Bean
    public IdempotentLimiter idempotentLimiter() {
        return new IdempotentLimiter();
    }
}
