package com.github.yizzuide.milkomeda.atom;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

/**
 * RedisAtomLockStrategy
 *
 * @author yizzuide
 * @since 3.3.0
 * Create at 2020/04/30 17:09
 */
@AllArgsConstructor
@Data
public class RedisAtomLockStrategy implements AtomLockStrategy {
    /**
     * Redisson API
     */
    private RedissonClient redisson;

    @Override
    public void lock(String keyPath, Duration leaseTime) {
        RLock lock = redisson.getLock(keyPath);
        lock.lock(leaseTime.toMillis(), TimeUnit.MILLISECONDS);
    }

    @Override
    public boolean tryLock(String keyPath, Duration waitTime, Duration leaseTime) throws InterruptedException {
        RLock lock = redisson.getLock(keyPath);
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
}
