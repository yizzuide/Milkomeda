/*
 * Copyright (c) 2021 yizzuide All rights Reserved.
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.github.yizzuide.milkomeda.atom;

import com.github.yizzuide.milkomeda.universe.function.Callback;

import java.time.Duration;

/**
 * Lock interface with default auto lock.
 *
 * @author yizzuide
 * @since 3.3.0
 * @version 3.20.0
 * <br>
 * Create at 2020/04/30 16:10
 */
public interface Atom {

    /**
     * auto lock with throw {@link AtomLockWaitTimeoutException} if wait timeout.
     * @param keyPath   lock key
     * @param type  lock type
     * @param waitTime  lock wait time
     * @param callback  lock success callback
     * @return  execute result
     * @throws Throwable execute exception
     * @since 3.20.0
     */
    default <E> E autoLock(String keyPath, AtomLockType type, long waitTime, Callback callback) throws Throwable {
        return this.autoLock(keyPath, type, false, waitTime, 60000L, AtomLockWaitTimeoutType.THROW_EXCEPTION, null, callback);
    }

    /**
     * auto lock with full config.
     * @param keyPath   lock key
     * @param type  lock type
     * @param readOnly  lock is read only
     * @param waitTime  lock wait time
     * @param leaseTime lock lease time
     * @param waitTimeoutType   lock wait timeout type
     * @param fallback  lock wait timeout fallback for {@link AtomLockWaitTimeoutType#FALLBACK}
     * @param callback  lock success callback
     * @return  execute result
     * @throws Throwable execute exception
     * @since 3.20.0
     */
    @SuppressWarnings("unchecked")
    default <E> E autoLock(String keyPath, AtomLockType type, boolean readOnly,
                          long waitTime, long leaseTime, AtomLockWaitTimeoutType waitTimeoutType, Callback fallback,
                            Callback callback) throws Throwable {
        Object lock = null;
        boolean isLocked = false;
        try {
            AtomLockInfo lockInfo = this.tryLock(keyPath, type, readOnly);
            isLocked = lockInfo.isLocked();
            lock = lockInfo.getLock();
            if (!isLocked) {
                lockInfo = this.tryLock(keyPath, Duration.ofMillis(waitTime), Duration.ofMillis(leaseTime), type, readOnly);
                isLocked = lockInfo.isLocked();
                lock = lockInfo.getLock();
            }
            if (isLocked) {
                return (E) callback.call();
            }
            // has set wait time?
            if (waitTime > 0) {
                if (waitTimeoutType == AtomLockWaitTimeoutType.THROW_EXCEPTION) {
                    throw new AtomLockWaitTimeoutException();
                } else if (waitTimeoutType == AtomLockWaitTimeoutType.FALLBACK) {
                    return (E) fallback.call();
                }
                // here is AtomLockWaitTimeoutType.WAIT_INFINITE
            }
            lockInfo = this.lock(keyPath, Duration.ofMillis(leaseTime), type, readOnly);
            lock = lockInfo.getLock();
            isLocked = lockInfo.isLocked();
            return (E) callback.call();
        } finally {
            // 只有加锁的线程需要解锁
            if (isLocked && lock != null && this.isLocked(lock)) {
                this.unlock(lock);
            }
        }
    }

    /**
     * 加锁，等待一定时间自动解锁
     * @param keyPath   键路径
     * @param leaseTime 获取锁后的解锁时间
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
     * 在等待时间内尝试去获取锁
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
     * @since 3.3.1
     */
    boolean isLocked(Object lock);
}
