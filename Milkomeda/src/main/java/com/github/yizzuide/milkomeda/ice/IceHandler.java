package com.github.yizzuide.milkomeda.ice;

import org.springframework.stereotype.Component;

import java.lang.annotation.*;

/**
 * IceHandler
 * 消息处理组件注解
 *
 * @author yizzuide
 * @since 1.15.0
 * Create at 2019/11/17 17:22
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
@Inherited
@Component
public @interface IceHandler {
}
