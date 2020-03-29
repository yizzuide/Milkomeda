package com.github.yizzuide.milkomeda.comet.core;

import com.github.yizzuide.milkomeda.comet.collector.EnableCometCollector;
import com.github.yizzuide.milkomeda.pulsar.EnablePulsar;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

/**
 * EnableComet
 *
 * @author yizzuide
 * @since 2.0.0
 * @version 2.8.0
 * Create at 2019/12/13 00:56
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
@EnablePulsar
@EnableCometCollector
@Import(CometConfig.class)
public @interface EnableComet {
}
