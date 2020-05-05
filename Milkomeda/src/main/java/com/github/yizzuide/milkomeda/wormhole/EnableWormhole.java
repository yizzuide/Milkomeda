package com.github.yizzuide.milkomeda.wormhole;

import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

/**
 * EnableWormhole
 *
 * @author yizzuide
 * @since 3.3.0
 * Create at 2020/05/05 13:57
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
@Import(WormholeConfig.class)
public @interface EnableWormhole {
}
