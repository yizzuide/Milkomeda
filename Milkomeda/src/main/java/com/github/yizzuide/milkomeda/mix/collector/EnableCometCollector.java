package com.github.yizzuide.milkomeda.mix.collector;

import com.github.yizzuide.milkomeda.comet.EnableComet;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

/**
 * EnableCometCollector
 *
 * @author yizzuide
 * @since 2.8.0
 * Create at 2020/03/28 18:55
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
@EnableComet
@Import(CometCollectorConfig.class)
public @interface EnableCometCollector {
}
