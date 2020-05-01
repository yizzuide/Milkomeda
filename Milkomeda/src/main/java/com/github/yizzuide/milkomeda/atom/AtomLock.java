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
    /**
     * 分布式key，支持Spring EL
     * @return String
     */
    String key() default "";

    /**
     * 等待获取锁时间ms
     * @return -1等待直到获取锁
     */
    long waitTime() default -1;

    /**
     * 自动释放锁时间ms
     * @return -1不自动释放锁
     */
    long leaseTime() default 60000;

    /**
     * 加锁类型
     * @return AtomLockType
     */
    AtomLockType type() default AtomLockType.FAIR;

    /**
     * 是否只读（仅支持读写锁类型 {@link AtomLockType#READ_WRITE}
     * @return true只读
     */
    boolean readOnly() default false;
}
