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

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.locks.InterProcessMutex;
import org.apache.curator.framework.recipes.locks.InterProcessReadWriteLock;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.PostConstruct;
import java.time.Duration;
import java.util.concurrent.TimeUnit;

/**
 * ZkAtom
 *
 * @author yizzuide
 * @since 3.3.0
 * @version 3.3.1
 * Create at 2020/05/01 12:26
 */
public class ZkAtom implements Atom {
    // 节点分隔符
    private final static String SEPARATOR = "/";
    // 根节点
    private static String ROOT_LOCK_NODE;

    @Autowired
    private AtomProperties props;

    @Autowired
    private CuratorFramework client;

    @PostConstruct
    public void init() {
        ROOT_LOCK_NODE = SEPARATOR + props.getZk().getRootLockNode();
    }

    @Override
    public AtomLockInfo lock(String keyPath, Duration leaseTime, AtomLockType type, boolean readOnly) throws Exception {
        if (type == AtomLockType.READ_WRITE) {
            return sharedReentrantReadWriteLock(keyPath, -1, null, readOnly);
        }
        return sharedReentrantLock(keyPath, -1, null);
    }

    // 不支持立即获取锁
    @Override
    public AtomLockInfo tryLock(String keyPath, AtomLockType type, boolean readOnly) {
        return AtomLockInfo.builder().isLocked(false).lock(null).build();
    }

    @Override
    public AtomLockInfo tryLock(String keyPath, Duration waitTime, Duration leaseTime, AtomLockType type, boolean readOnly) throws Exception {
        if (type == AtomLockType.READ_WRITE) {
            return sharedReentrantReadWriteLock(keyPath, waitTime.toMillis(), TimeUnit.MILLISECONDS, readOnly);
        }
       return sharedReentrantLock(keyPath, waitTime.toMillis(), TimeUnit.MILLISECONDS);
    }

    @Override
    public void unlock(Object lock) throws Exception {
        sharedReentrantUnLock(lock);
    }

    @Override
    public boolean isLocked(Object lock) {
        return ((InterProcessMutex)lock).isOwnedByCurrentThread();
    }

    // 可重入排它公平锁
    private AtomLockInfo sharedReentrantLock(String keyPath, long time, TimeUnit unit) throws Exception {
        String lockPath = ROOT_LOCK_NODE + SEPARATOR + keyPath;
        // 获取底层ZooKeeper Client API
        // ZooKeeper zooKeeper = client.getZookeeperClient().getZooKeeper();
        // Shared Reentrant Lock
        InterProcessMutex lock = new InterProcessMutex(client, lockPath);
        boolean isLocked = lock.acquire(time, unit);
        return AtomLockInfo.builder().isLocked(isLocked).lock(lock).build();
    }

    // 可重入读写公平锁
    private AtomLockInfo sharedReentrantReadWriteLock(String keyPath, long time, TimeUnit unit, boolean readOnly) throws Exception {
        String lockPath = ROOT_LOCK_NODE + SEPARATOR + keyPath;
        InterProcessReadWriteLock rwLock = new InterProcessReadWriteLock(client, lockPath);
        InterProcessMutex lock = readOnly ? rwLock.readLock() : rwLock.writeLock();
        boolean isLocked = lock.acquire(time, unit);
        return AtomLockInfo.builder().isLocked(isLocked).lock(lock).build();
    }

    private void sharedReentrantUnLock(Object lock) throws Exception {
        ((InterProcessMutex)lock).release();
    }
}
