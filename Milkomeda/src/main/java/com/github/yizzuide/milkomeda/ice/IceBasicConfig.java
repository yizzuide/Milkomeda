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
 * @since 3.0.8
 * Create at 2019/11/21 11:16
 */
@Import(MilkomedaContextConfig.class)
@ConditionalOnClass(RedisTemplate.class)
@AutoConfigureAfter(RedisAutoConfiguration.class)
@EnableConfigurationProperties(IceProperties.class)
public class IceBasicConfig {

    // 注入需要需要使用的ApplicationContext（让MilkomedaContextConfig先配置）
    @SuppressWarnings("unused")
    @Autowired
    private ApplicationContextHolder applicationContextHolder;

    @Autowired
    private IceProperties props;

    @Bean
    @ConditionalOnMissingBean(JobPool.class)
    public JobPool jobPool() {
        return new RedisJobPool(props);
    }

    @Bean
    @ConditionalOnMissingBean(DelayBucket.class)
    public DelayBucket delayBucket() {
        RedisDelayBucket delayBucket = new RedisDelayBucket(props);
        IceHolder.setDelayBucket(delayBucket);
        return delayBucket;
    }

    @Bean
    @ConditionalOnMissingBean(ReadyQueue.class)
    public ReadyQueue readyQueue() {
        return new RedisReadyQueue(props);
    }

    @Bean
    @ConditionalOnMissingBean(DeadQueue.class)
    public DeadQueue deadQueue() {
        RedisDeadQueue deadQueue = new RedisDeadQueue(props);
        IceHolder.setDeadQueue(deadQueue);
        return deadQueue;
    }

    @Bean
    @ConditionalOnMissingBean(Ice.class)
    public Ice redisIce() {
        RedisIce redisIce = new RedisIce(props);
        IceHolder.setIce(redisIce);
        return redisIce;
    }
}
