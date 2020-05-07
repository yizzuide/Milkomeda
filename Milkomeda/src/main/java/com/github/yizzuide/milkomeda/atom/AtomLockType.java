package com.github.yizzuide.milkomeda.atom;

/**
 * AtomLockType
 *
 * @author yizzuide
 * @since 3.3.0
 * Create at 2020/05/01 13:50
 */
public enum AtomLockType {
    /**
     * 非公平锁
     */
    NON_FAIR,
    /**
     * 公平锁
     */
    FAIR,
    /**
     * 读写锁
     */
    READ_WRITE
}
