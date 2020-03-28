package com.github.yizzuide.milkomeda.moon;

import com.github.yizzuide.milkomeda.pulsar.EnablePulsar;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

/**
 * EnableMoon
 *
 * @author yizzuide
 * @since 2.8.0
 * Create at 2020/03/28 17:40
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
@EnablePulsar
@Import(MoonConfig.class)
public @interface EnableMoon {
}
