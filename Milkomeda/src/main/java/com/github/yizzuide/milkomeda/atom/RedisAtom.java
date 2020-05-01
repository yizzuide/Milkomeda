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
 * Create at 2020/04/30 17:09
 */
@AllArgsConstructor
@Data
public class RedisAtom implements Atom {

    private RedissonClient redisson;

    @Override
    public void lock(String keyPath, Duration leaseTime) {
        lock(keyPath, leaseTime, AtomLockType.NON_FAIR, false);
    }

    @Override
    public boolean tryLock(String keyPath, AtomLockType type, boolean readOnly) {
        RLock lock = getLock(keyPath, type, readOnly);
        // 默认leaseTime为30s，并自动续约
        return lock.tryLock();
    }

    @Override
    public void lock(String keyPath, Duration leaseTime, AtomLockType type, boolean readOnly) {
        RLock lock = getLock(keyPath, type, readOnly);
        lock.lock(leaseTime.toMillis(), TimeUnit.MILLISECONDS);
    }

    @Override
    public boolean tryLock(String keyPath, Duration waitTime, Duration leaseTime) throws InterruptedException {
        return tryLock(keyPath, waitTime, leaseTime, AtomLockType.NON_FAIR, false);
    }


    @Override
    public boolean tryLock(String keyPath, Duration waitTime, Duration leaseTime, AtomLockType type, boolean readOnly) throws InterruptedException {
        RLock lock = getLock(keyPath, type, readOnly);
        return lock.tryLock(waitTime.toMillis(), waitTime.toMillis(), TimeUnit.MILLISECONDS);
    }

    @Override
    public void unlock(String keyPath) {
        RLock lock = redisson.getLock(keyPath);
        lock.unlock();
    }

    @Override
    public boolean forceUnlock(String keyPath) {
        RLock lock = redisson.getLock(keyPath);
        return lock.forceUnlock();
    }

    @Override
    public boolean isLocked(String keyPath) {
        RLock lock = redisson.getLock(keyPath);
        return lock.isLocked();
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
