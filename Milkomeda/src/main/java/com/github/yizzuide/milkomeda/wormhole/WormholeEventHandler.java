package com.github.yizzuide.milkomeda.wormhole;

import org.springframework.stereotype.Component;

import java.lang.annotation.*;

/**
 * DomainEventHandler
 * 领域事件处理器
 *
 * @author yizzuide
 * @since 3.3.0
 * Create at 2020/05/05 14:14
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
@Inherited
@Component
public @interface WormholeEventHandler {
}
