package com.github.yizzuide.milkomeda.pulsar;

import org.springframework.context.annotation.Import;
import org.springframework.scheduling.annotation.EnableAsync;

import java.lang.annotation.*;

/**
 * EnablePulsar
 *
 * @author yizzuide
 * @since 1.16.0
 * Create at 2019/11/23 00:18
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
@EnableAsync
@Import(PulsarConfig.class)
public @interface EnablePulsar {
}
