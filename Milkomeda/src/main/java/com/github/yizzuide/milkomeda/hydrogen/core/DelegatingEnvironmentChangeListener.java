package com.github.yizzuide.milkomeda.hydrogen.core;

import com.github.yizzuide.milkomeda.universe.context.ApplicationContextHolder;
import org.springframework.cloud.context.environment.EnvironmentChangeEvent;
import org.springframework.context.event.EventListener;

/**
 * DelegatingEnvironmentChangeListener
 * 桥接到Spring Cloud EnvironmentChangeEvent的监听器
 *
 * @author yizzuide
 * @since 3.2.0
 * Create at 2020/04/24 18:43
 */
public class DelegatingEnvironmentChangeListener {

    @EventListener
    public void configListener(EnvironmentChangeEvent event) {
        DelegatingEnvironmentChangeEvent environmentChangeEvent = new DelegatingEnvironmentChangeEvent(event);
        ApplicationContextHolder.get().publishEvent(environmentChangeEvent);
    }
}
