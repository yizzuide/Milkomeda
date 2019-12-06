package com.github.yizzuide.milkomeda.rock;

import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
@Import(RockConfig.class)
public @interface EnableRock {

}
