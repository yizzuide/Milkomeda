package com.github.yizzuide.milkomeda.crust;

import org.springframework.context.annotation.Import;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;

import java.lang.annotation.*;

/**
 * EnableCrust
 *
 * @author yizzuide
 * @since 1.14.0
 * @version 1.16.2
 * Create at 2019/11/11 15:14
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
@Import(CrustConfig.class)
public @interface EnableCrust {
}
