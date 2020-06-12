package com.github.yizzuide.milkomeda.ice;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.lang.NonNull;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

/**
 * DelayTimer
 *
 * @author yizzuide
 * @since 1.15.0
 * @version 3.7.2
 * Create at 2019/11/16 18:57
 */
public class DelayTimer implements ApplicationListener<ApplicationStartedEvent> {

    @Autowired
    private IceProperties props;

    @Autowired
    private ThreadPoolTaskScheduler taskScheduler;

    @Autowired
    private DelegatingDelayJobHandler delegatingDelayJobHandler;

    @Override
    public void onApplicationEvent(@NonNull ApplicationStartedEvent contextRefreshedEvent) {
        taskScheduler.scheduleWithFixedDelay(delegatingDelayJobHandler, props.getDelayBucketPollRate());
    }
}
