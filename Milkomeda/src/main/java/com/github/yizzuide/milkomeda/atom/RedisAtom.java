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

import lombok.AllArgsConstructor;
import lombok.Data;
import org.redisson.api.RLock;
import org.redisson.api.RReadWriteLock;
import org.redisson.api.RedissonClient;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

/**
 * RedisAtom
 *
 * @author yizzuide
 * @since 3.3.0
 * @version 3.3.1
 * <br>
 * Create at 2020/04/30 17:09
 */
@AllArgsConstructor
@Data
public class RedisAtom implements Atom {

    private RedissonClient redisson;

    @Override
    public AtomLockInfo lock(String keyPath, Duration leaseTime, AtomLockType type, boolean readOnly) {
        RLock lock = getLock(keyPath, type, readOnly);
        lock.lock(leaseTime.toMillis(), TimeUnit.MILLISECONDS);
        return AtomLockInfo.builder().isLocked(true).lock(lock).build();
    }

    @Override
    public AtomLockInfo tryLock(String keyPath, AtomLockType type, boolean readOnly) {
        RLock lock = getLock(keyPath, type, readOnly);
        boolean isLocked = lock.tryLock();
        return AtomLockInfo.builder().isLocked(isLocked).lock(lock).build();
    }


    @Override
    public AtomLockInfo tryLock(String keyPath, Duration waitTime, Duration leaseTime, AtomLockType type, boolean readOnly) throws InterruptedException {
        RLock lock = getLock(keyPath, type, readOnly);
        boolean isLocked = lock.tryLock(waitTime.toMillis(), leaseTime.toMillis(), TimeUnit.MILLISECONDS);
        return AtomLockInfo.builder().isLocked(isLocked).lock(lock).build();
    }

    @Override
    public void unlock(Object lock) {
        ((RLock) lock).unlock();
    }

    @Override
    public boolean isLocked(Object lock) {
        // 这个方法只检测了key是否在redis存在，不可靠！
        // return ((RLock) lock).isLocked();
        return ((RLock) lock).isHeldByCurrentThread();
    }

    private RLock getLock(String keyPath, AtomLockType type, boolean readOnly) {
        switch (type) {
            case FAIR:
                return redisson.getFairLock(keyPath);
            case READ_WRITE:
                RReadWriteLock readWriteLock = redisson.getReadWriteLock(keyPath);
                return readOnly ? readWriteLock.readLock() : readWriteLock.writeLock();
            default:
                return redisson.getLock(keyPath);
        }
    }
}
