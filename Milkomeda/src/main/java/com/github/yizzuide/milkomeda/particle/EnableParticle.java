package com.github.yizzuide.milkomeda.particle;

import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

/**
 * EnableParticle
 *
 * @author yizzuide
 * @since 2.0.0
 * Create at 2019/12/13 00:57
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
@Import(ParticleConfig.class)
public @interface EnableParticle {
}
