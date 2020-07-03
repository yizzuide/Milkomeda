package com.github.yizzuide.milkomeda.pillar;

import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

/**
 * EnablePillar
 *
 * @author yizzuide
 * @since 3.10.0
 * Create at 2020/07/02 16:24
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
@Import(PillarConfig.class)
public @interface EnablePillar {
}
