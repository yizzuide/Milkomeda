package com.github.yizzuide.milkomeda.atom;

import java.time.Duration;

/**
 * AtomLockStrategy
 *
 * @author yizzuide
 * @since 3.3.0
 * Create at 2020/04/30 16:10
 */
public interface AtomLockStrategy {

    /**
     * 加锁，必需手动调用unlock解锁
     * @param keyPath 键路径
     */
    default void lock(String keyPath) {
        lock(keyPath, Duration.ofSeconds(-1));
    }

    /**
     * 加锁，等待一定时间自动解锁
     * @param keyPath 键路径
     * @param leaseTime 解锁时间
     */
    void lock(String keyPath, Duration leaseTime);

    /**
     * 尝试去获取锁
     * @param keyPath 键路径
     * @param waitTime  尝试时间
     * @param leaseTime 获取锁后的解锁时间
     * @return 是否获取成功
     * @throws InterruptedException 加锁打断异常
     */
    boolean tryLock(String keyPath, Duration waitTime, Duration leaseTime) throws InterruptedException;

    /**
     * 解锁
     * @param keyPath 键路径
     */
    void unlock(String keyPath);

    /**
     * 强制解锁，不管锁的状态
     * @param keyPath 键路径
     * @return 是否解锁成功
     */
    boolean forceUnlock(String keyPath);

    /**
     * 判断是否加锁
     * @param keyPath 键路径
     * @return 是否加锁
     */
    boolean isLocked(String keyPath);
}
