package com.github.yizzuide.milkomeda.atom;

/**
 * AtomLockWaitTimeoutType
 * 锁等待超时处理类型
 *
 * @author yizzuide
 * @since 3.3.1
 * Create at 2020/05/08 13:46
 */
public enum AtomLockWaitTimeoutType {
    /**
     * 抛出异常
     */
    THROW_EXCEPTION,
    /**
     * 无限等待
     */
    WAIT_INFINITE,
    /**
     * 自定义反馈处理
     */
    FALLBACK
}
