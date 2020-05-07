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

    // 获取红锁
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
