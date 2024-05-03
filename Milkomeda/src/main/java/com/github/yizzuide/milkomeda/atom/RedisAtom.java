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

    // Redisson解决锁重入问题：数据类型采用 Hash<key, [锁持有ID，重入次数]>，当删除时检测重入次数为 1 时才删除。
    // Redisson解决锁提前释放问题：通过 WatchDog 每隔锁释放时间的 1/3 检查当前是否持有锁，持有就重置锁释放时间为初始设置的值。
    // Redisson解决分布式服务线程的并发同步：通过 redis 发布/订阅来唤醒其它阻塞的线程，被唤醒（它的子线程操作信号量来唤醒）的线程都去抢占这把锁，没抢到的的线程继续阻塞并通过子线程订阅锁释放事件。
    // Redisson不完全解决redis多节点同步：通过 RedLook 实现在半数以上节点设置成功才算加锁成功，但由于 Redis 的主从复制是通过fork子进程来增量复制的，如果当前节点没有同步完锁就挂了就丢失了加锁数据问题。
    //  redis是 AP 模式，主要保证访问的可靠性，如果需要数据强一致就用 CP 模式的 Zookeeper。
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
