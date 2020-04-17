package com.github.yizzuide.milkomeda.demo;

import com.github.yizzuide.milkomeda.ice.IceHolder;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

/**
 * MilkomedaApplicationListener
 *
 * @author yizzuide
 * Create at 2020/04/16 18:24
 */
@Component
public class MilkomedaApplicationListener implements ApplicationListener<ContextRefreshedEvent> {
    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        IceHolder.setInstanceName("product");
        IceHolder.activeDeadJobs();
    }
}
