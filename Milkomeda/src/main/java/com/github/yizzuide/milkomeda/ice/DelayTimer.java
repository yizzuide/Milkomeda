package com.github.yizzuide.milkomeda.ice;

import com.github.yizzuide.milkomeda.universe.context.ApplicationContextHolder;
import com.github.yizzuide.milkomeda.universe.context.WebContext;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

import java.util.ArrayList;
import java.util.List;

/**
 * DelayTimer
 *
 * @author yizzuide
 * @since 1.15.0
 * @version 3.0.8
 * Create at 2019/11/16 18:57
 */
public class DelayTimer implements InitializingBean, ApplicationListener<ContextRefreshedEvent> {

    @Autowired
    private JobPool jobPool;

    @Autowired
    private DelayBucket delayBucket;

    @Autowired
    private ReadyQueue readyQueue;

    @Autowired
    private DeadQueue deadQueue;

    @Autowired
    private IceProperties props;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private ThreadPoolTaskScheduler taskScheduler;

    // 启动标识
    private boolean startup;

    // 延迟桶处理器
    private List<DelayJobHandler> delayJobHandlers;

    @Override
    public void onApplicationEvent(ContextRefreshedEvent contextRefreshedEvent) {
        if (startup) return;
        // 启动Timer
        for (DelayJobHandler delayJobHandler : delayJobHandlers) {
            taskScheduler.scheduleWithFixedDelay(delayJobHandler, props.getDelayBucketPollRate());
        }
        startup = true;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        delayJobHandlers = new ArrayList<>();
        for (int i = 0; i < props.getDelayBucketCount(); i++) {
            // 注册为bean，让其可以接收Spring事件
            DelayJobHandler delayJobHandler = WebContext.registerBean((ConfigurableApplicationContext) ApplicationContextHolder.get(), "delayJobHandler" + i, DelayJobHandler.class);
            delayJobHandler.fill(redisTemplate, jobPool, delayBucket, readyQueue, deadQueue, i, props);
            delayJobHandlers.add(delayJobHandler);
        }
    }
}
