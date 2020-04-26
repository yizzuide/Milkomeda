package com.github.yizzuide.milkomeda.comet.core;

import com.github.yizzuide.milkomeda.comet.collector.CometCollectorConfig;
import com.github.yizzuide.milkomeda.comet.logger.CometLoggerConfig;
import com.github.yizzuide.milkomeda.pulsar.EnablePulsar;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

/**
 * EnableComet
 *
 * @author yizzuide
 * @since 2.0.0
 * @version 3.0.0
 * Create at 2019/12/13 00:56
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
@EnablePulsar
@Import({CometConfig.class, CometCollectorConfig.class, CometLoggerConfig.class})
public @interface EnableComet {
}
