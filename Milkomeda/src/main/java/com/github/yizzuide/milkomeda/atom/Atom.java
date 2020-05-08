package com.github.yizzuide.milkomeda.atom;

import java.time.Duration;

/**
 * Atom
 *
 * @author yizzuide
 * @since 3.3.0
 * @version 3.4.0
 * Create at 2020/04/30 16:10
 */
public interface Atom {

    /**
     * 加锁，等待一定时间自动解锁
     * @param keyPath   键路径
     * @param leaseTime 解析时间
     * @param type      锁类型
     * @param readOnly  只读（只在读写锁有效）
     * @return  AtomLockInfo
     * @throws Exception 加锁异常
     */
    AtomLockInfo lock(String keyPath, Duration leaseTime, AtomLockType type, boolean readOnly) throws Exception;

    /**
     * 尝试立即获得一次锁
     * @param keyPath   键路径
     * @param type      锁类型
     * @param readOnly  只读（只在读写锁有效）
     * @return AtomLockInfo
     * @throws Exception 加锁异常
     */
    AtomLockInfo tryLock(String keyPath, AtomLockType type, boolean readOnly) throws Exception;

    /**
     * 尝试去获取锁
     * @param keyPath   键路径
     * @param waitTime  尝试时间
     * @param leaseTime 获取锁后的解锁时间
     * @param type      锁类型
     * @param readOnly  只读（只在读写锁有效）
     * @return AtomLockInfo
     * @throws Exception 加锁打断异常
     */
    AtomLockInfo tryLock(String keyPath, Duration waitTime, Duration leaseTime, AtomLockType type, boolean readOnly) throws Exception;

    /**
     * 解锁
     * @param lock 线程绑定的锁对象
     * @throws Exception 释放锁异常
     */
    void unlock(Object lock) throws Exception;

    /**
     * 当前线程是否加锁
     * @param lock  线程绑定的锁对象
     * @return  true已加锁
     * @since 3.4.0
     */
    boolean isLocked(Object lock);
}
