package com.github.yizzuide.milkomeda.halo;

import org.springframework.stereotype.Component;

import java.lang.annotation.*;

/**
 * HaloHandler
 * 持久化事件处理器
 *
 * @author yizzuide
 * @since 2.5.0
 * Create at 2020/01/30 22:35
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
@Inherited
@Component
public @interface HaloHandler {
}
