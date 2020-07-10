package com.github.yizzuide.milkomeda.wormhole;

import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.*;

/**
 * DomainAction
 * 领域事件动作
 *
 * @author yizzuide
 * @since 3.3.0
 * Create at 2020/05/05 14:20
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
public @interface WormholeAction {
    /**
     * 监听的Action
     * @return action name
     */
    @AliasFor("name")
    String value() default "";

    /**
     * 监听的Action
     * @return action name
     */
    @AliasFor("value")
    String name() default "";

    /**
     * 事务回调执行
     * @return  WormholeTransactionType
     */
    WormholeTransactionHangType transactionHang() default WormholeTransactionHangType.NONE;
}
