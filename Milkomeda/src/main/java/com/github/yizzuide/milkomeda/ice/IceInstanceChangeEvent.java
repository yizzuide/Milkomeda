package com.github.yizzuide.milkomeda.ice;

import org.springframework.context.ApplicationEvent;

/**
 * IceInstanceChangeEvent
 * 实例名更改事件
 *
 * @author yizzuide
 * @since 3.0.7
 * Create at 2020/04/16 16:00
 */
public class IceInstanceChangeEvent extends ApplicationEvent {
    public IceInstanceChangeEvent(Object source) {
        super(source);
    }
}
