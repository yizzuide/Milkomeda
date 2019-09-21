package com.github.yizzuide.milkomeda.echo;

import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

/**
 * EnableEcho
 * 注解方式启用Echo模块
 *
 * @author yizzuide
 * @since 1.13.0
 * Create at 2019/09/21 18:38
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
@Import({EchoConfig.class})
public @interface EnableEcho {
}
