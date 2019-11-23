package com.github.yizzuide.milkomeda.ice;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

/**
 * DelayTimer
 *
 * @author yizzuide
 * @since 1.15.0
 * @version 1.16.0
 * Create at 2019/11/16 18:57
 */
public class DelayTimer implements ApplicationListener<ContextRefreshedEvent> {

    @Autowired
    private JobPool jobPool;

    @Autowired
    private DelayBucket delayBucket;

    @Autowired
    private ReadyQueue readyQueue;

    @Autowired
    private IceProperties props;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private ThreadPoolTaskScheduler taskScheduler;

    @Override
    public void onApplicationEvent(ContextRefreshedEvent contextRefreshedEvent) {
        // 启动Timer
        for (int i = 0; i < props.getDelayBucketCount(); i++) {
            taskScheduler.scheduleWithFixedDelay(new DelayJobHandler(redisTemplate, jobPool, delayBucket, readyQueue, i, props),
                    props.getDelayBucketPollRate());
        }
    }
}
