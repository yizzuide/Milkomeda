package com.github.yizzuide.milkomeda.atom;

import java.time.Duration;

/**
 * ZkAtom
 *
 * @author yizzuide
 * Create at 2020/05/01 12:26
 */
public class ZkAtom implements Atom {


    @Override
    public void lock(String keyPath, Duration leaseTime) {

    }

    @Override
    public void lock(String keyPath, Duration leaseTime, AtomLockType type, boolean readOnly) {

    }

    @Override
    public boolean tryLock(String keyPath, AtomLockType type, boolean readOnly) throws InterruptedException {
        return false;
    }

    @Override
    public boolean tryLock(String keyPath, Duration waitTime, Duration leaseTime) throws InterruptedException {
        return false;
    }

    @Override
    public boolean tryLock(String keyPath, Duration waitTime, Duration leaseTime, AtomLockType type, boolean readOnly) throws InterruptedException {
        return false;
    }

    @Override
    public void unlock(String keyPath) {

    }

    @Override
    public boolean forceUnlock(String keyPath) {
        return false;
    }

    @Override
    public boolean isLocked(String keyPath) {
        return false;
    }
}
