package com.github.yizzuide.milkomeda.hydrogen.core;

import org.springframework.cloud.context.environment.EnvironmentChangeEvent;
import org.springframework.context.ApplicationEvent;

import java.util.Set;

/**
 * DelegatingEnvironmentChangeEvent
 *
 * @author yizzuide
 * @since 3.2.0
 * Create at 2020/04/24 18:42
 */
public class DelegatingEnvironmentChangeEvent extends ApplicationEvent {
    // 适配Spring Cloud Context事件
    private EnvironmentChangeEvent event;

    private static final long serialVersionUID = 1421963671133139510L;

    public DelegatingEnvironmentChangeEvent(EnvironmentChangeEvent event) {
        super(event.getSource());
        this.event = event;
    }

    public Set<String> getKeys() {
        return event.getKeys();
    }
}
