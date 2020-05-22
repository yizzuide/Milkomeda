package com.github.yizzuide.milkomeda.metal;

import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

/**
 * EnableMetal
 *
 * @author yizzuide
 * @since 3.6.0
 * Create at 2020/05/21 23:26
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
@Inherited
@Import({MetalConfig.class, RedisMetalConfig.class})
public @interface EnableMetal {
}
