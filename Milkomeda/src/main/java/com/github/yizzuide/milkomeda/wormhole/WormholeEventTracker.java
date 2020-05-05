package com.github.yizzuide.milkomeda.wormhole;

import org.springframework.stereotype.Component;

import java.lang.annotation.*;

/**
 * WormholeEventTracker
 * 领域事件跟踪器注解
 *
 * @author yizzuide
 * @since 3.3.0
 * @see WormholeEventTrack
 * Create at 2020/05/05 14:56
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
@Inherited
@Component
public @interface WormholeEventTracker {
}
