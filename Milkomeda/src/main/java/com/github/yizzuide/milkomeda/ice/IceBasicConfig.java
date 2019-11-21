package com.github.yizzuide.milkomeda.ice;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;

/**
 * IceBasicConfig
 * 基础配置
 *
 * @author yizzuide
 * @since 1.15.2
 * Create at 2019/11/21 11:16
 */
public class IceBasicConfig {
    @Bean
    @ConditionalOnMissingBean(JobPool.class)
    public JobPool jobPool() {
        return new RedisJobPool();
    }

    @Bean
    @ConditionalOnMissingBean(DelayBucket.class)
    public DelayBucket delayBucket() {
        return new RedisDelayBucket();
    }

    @Bean
    @ConditionalOnMissingBean(ReadyQueue.class)
    public ReadyQueue readyQueue() {
        return new RedisReadyQueue();
    }

    @Bean
    @ConditionalOnMissingBean(Ice.class)
    public Ice redisIce() {
        return new RedisIce();
    }
}
