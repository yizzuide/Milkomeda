package com.github.yizzuide.milkomeda.comet.collector;

import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

/**
 * EnableCometCollector
 *
 * @author yizzuide
 * @since 3.0.0
 * Create at 2020/03/28 18:55
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
@Import(CometCollectorConfig.class)
public @interface EnableCometCollector {
}
