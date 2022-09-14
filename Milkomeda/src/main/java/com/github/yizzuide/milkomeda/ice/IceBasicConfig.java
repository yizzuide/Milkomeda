/*
 * Copyright (c) 2021 yizzuide All rights Reserved.
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.github.yizzuide.milkomeda.ice;

import com.github.yizzuide.milkomeda.universe.config.MilkomedaContextConfig;
import com.github.yizzuide.milkomeda.universe.context.ApplicationContextHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
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
 * @since 3.14.0
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

    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    @Autowired
    private IceProperties props;

    @Bean
    @ConditionalOnMissingBean(JobPool.class)
    public JobPool jobPool() {
        IceHolder.setProps(props);
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

    @Bean
    @ConditionalOnProperty(prefix = "milkomeda.ice", name = "enable-introspect", havingValue = "true")
    public JobInspector jobInspector() {
        JobInspector jobInspector = new RedisJobInspector(props);
        IceHolder.setJobInspector(jobInspector);
        return jobInspector;
    }
}
