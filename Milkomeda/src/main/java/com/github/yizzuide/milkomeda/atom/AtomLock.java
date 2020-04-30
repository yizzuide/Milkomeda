package com.github.yizzuide.milkomeda.atom;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * AtomLock
 *
 * @author yizzuide
 * @since 3.3.0
 * Create at 2020/04/30 16:26
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface AtomLock {
    String key() default "";

    /**
     * 等待获取锁时间ms
     * @return 获取锁时间ms
     */
    long waitTime() default 60000L;

    /**
     * 自动释放锁时间ms
     * @return 释放锁时间ms
     */
    long leaseTime() default 60000L;
}
