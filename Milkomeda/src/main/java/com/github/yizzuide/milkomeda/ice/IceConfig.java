package com.github.yizzuide.milkomeda.ice;

import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;

/**
 * IceConfig
 *
 * @author yizzuide
 * @since 1.15.0
 * Create at 2019/11/16 17:19
 */
@Configuration
@ConditionalOnClass(RedisTemplate.class)
@AutoConfigureAfter(RedisAutoConfiguration.class)
@EnableConfigurationProperties(IceProperties.class)
public class IceConfig {

    @Bean
    @ConditionalOnProperty(prefix = "milkomeda.ice", name = "enable-task", havingValue = "true")
    public IceContext iceContext() {
        return new IceContext();
    }

    @Bean
    public JobPool jobPool() {
        return new RedisJobPool();
    }

    @Bean
    public DelayBucket delayBucket() {
        return new RedisDelayBucket();
    }

    @Bean
    public ReadyQueue readyQueue() {
        return new RedisReadyQueue();
    }

    @Bean
    public Ice redisIce() {
        return new RedisIce();
    }

    @Bean
    public DelayTimer delayTimer() {
        return new DelayTimer();
    }
}
