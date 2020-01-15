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
 * @version 2.0.1
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

    // 启动标识
    private boolean startup;

    @Override
    public void onApplicationEvent(ContextRefreshedEvent contextRefreshedEvent) {
        if (startup) return;
        // 启动Timer
        for (int i = 0; i < props.getDelayBucketCount(); i++) {
            taskScheduler.scheduleWithFixedDelay(new DelayJobHandler(redisTemplate, jobPool, delayBucket, readyQueue, i, props),
                    props.getDelayBucketPollRate());
        }
        startup = true;
    }
}
