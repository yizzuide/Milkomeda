package com.github.yizzuide.milkomeda.ice;

import com.github.yizzuide.milkomeda.universe.config.MilkomedaContextConfig;
import com.github.yizzuide.milkomeda.universe.context.ApplicationContextHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.core.RedisTemplate;

/**
 * IceBasicConfig
 * 基础配置
 *
 * @author yizzuide
 * @since 1.15.2
 * Create at 2019/11/21 11:16
 */
@Import(MilkomedaContextConfig.class)
@ConditionalOnClass(RedisTemplate.class)
@AutoConfigureAfter(RedisAutoConfiguration.class)
@EnableConfigurationProperties(IceProperties.class)
public class IceBasicConfig {

    @Autowired
    private ApplicationContextHolder applicationContextHolder;

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
