/*
 * Copyright (c) 2023 yizzuide All rights Reserved.
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

import com.github.yizzuide.milkomeda.light.LightContext;
import com.github.yizzuide.milkomeda.light.Spot;
import com.github.yizzuide.milkomeda.universe.extend.env.Environment;
import io.etcd.jetcd.ByteSequence;
import io.etcd.jetcd.lock.LockResponse;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Etcd distributed lock.
 *
 * @since 3.15.0
 * @author yizzuide
 * <br>
 * Create at 2023/04/29 01:54
 */
@Slf4j
@SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
public class EtcdAtom implements Atom {

    @Autowired
    private EtcdClientInfo clientInfo;

    // Lock监控线程池调度器
    @Autowired
    private ThreadPoolTaskScheduler taskScheduler;

    // 线程锁上下文
    private final LightContext<String, LockData> lockLightContext = new LightContext<>();

    @Override
    public AtomLockInfo lock(String keyPath, Duration leaseTime, AtomLockType type, boolean readOnly) throws Exception {
        return tryLock(keyPath, null, leaseTime, type, readOnly);
    }

    @Override
    public AtomLockInfo tryLock(String keyPath, AtomLockType type, boolean readOnly) throws Exception {
        Spot<String, LockData> lockDataSpot = lockLightContext.get();
        LockData existsLockData = lockDataSpot == null ? null : lockDataSpot.getData();
        if (existsLockData != null && existsLockData.isLockSuccess()) {
            int lockCount = existsLockData.getLockCount().incrementAndGet();
            if (lockCount < 0) {
                throw new Error("Exceeded the limit of reentrant times for ETCD locks");
            }
            return AtomLockInfo.builder().isLocked(true).lock(existsLockData).build();
        }
        return AtomLockInfo.builder().isLocked(false).lock(null).build();
    }

    @Override
    public AtomLockInfo tryLock(String keyPath, Duration waitTime, Duration leaseTime, AtomLockType type, boolean readOnly) throws Exception {
        String lockKey = clientInfo.getRootLockNode() + "/" + keyPath;
        // 租约时间
        long leaseTTL = leaseTime.getSeconds();
        try {
            // 租约Id
            long leaseId = clientInfo.getLeaseClient().grant(leaseTTL).get().getID();
            // 续租心跳周期
            long period = leaseTTL - leaseTTL / 5;
            // 启动续约监控
            ScheduledFuture<?> scheduledFuture = taskScheduler.scheduleAtFixedRate(() -> clientInfo.getLeaseClient().keepAliveOnce(leaseId),
                    Instant.ofEpochMilli(0L),
                    Duration.ofSeconds(period));
            // 加锁
            LockResponse lockResponse = clientInfo.getLockClient().lock(ByteSequence.from(lockKey.getBytes()), leaseId).get();
            String lockPath = null;
            if (lockResponse != null) {
                lockPath = lockResponse.getKey().toString(StandardCharsets.UTF_8);
            }
            Thread currentThread = Thread.currentThread();
            LockData lockData = new LockData(lockKey, currentThread);
            int lockCount = lockData.getLockCount().incrementAndGet();
            lockData.setLeaseId(leaseId);
            lockData.setLockedKeyPath(lockPath);
            lockLightContext.set(new Spot<>(lockKey, lockData));
            lockData.setLockSuccess(true);
            lockData.setScheduledFuture(scheduledFuture);
            if (Environment.isShowLog()) {
                log.info("thread[{}] locked on key: {}, lock count: {}", currentThread.getName(), lockData.getLockedKey(), lockCount);
            }
            return AtomLockInfo.builder().isLocked(true).lock(lockData).build();
        } catch (InterruptedException | ExecutionException e) {
            log.error("Etcd lock error with msg: {}", e.getMessage(), e);
        }
        return AtomLockInfo.builder().isLocked(false).lock(null).build();
    }

    @Override
    public void unlock(Object lock) throws Exception {
        LockData lockData = (LockData) lock;
        int lockCount = lockData.getLockCount().decrementAndGet();
        if (lockCount > 0) {
            return;
        }
        if (lockCount < 0) {
            throw new AtomUnLockException("Abnormal unlocking status for ETCD locks");
        }
        try {
            // 解锁
            String lockKey = lockData.getLockKey();
            if (lockKey != null) {
                clientInfo.getLockClient().unlock(ByteSequence.from(lockKey.getBytes())).get();
            }
            // 关闭续约监控
            lockData.getScheduledFuture().cancel(true);
            // 删除锁
            if (lockData.getLeaseId() != 0L) {
                clientInfo.getLeaseClient().revoke(lockData.getLeaseId());
            }
            lockData.setLockSuccess(false);
            if (Environment.isShowLog()) {
                Thread currentThread = lockData.getCurrentThread();
                log.info("Thread[{}] closed lock with key: {}, lock count: {}", currentThread.getName(), lockData.getLockedKey(), lockCount);
            }
        } catch (InterruptedException | ExecutionException e) {
            log.error("Etcd unlock error with msg: {}", e.getMessage(), e);
        } finally {
            // 清空线程上下文
            lockLightContext.remove();
        }
    }

    @Override
    public boolean isLocked(Object lock) {
        LockData lockData = (LockData) lock;
        return lockData.isLockSuccess();
    }

    @Data
    static class LockData {
        /**
         * Remark lock count for support reentrant lock.
         */
        private AtomicInteger lockCount = new AtomicInteger(0);

        /**
         * Local thread which hold lock.
         */
        private Thread currentThread;

        /**
         * Etcd lease ID.
         */
        private long leaseId;

        /**
         * Etcd lock key.
         */
        private String lockKey;

        /**
         * Etcd locked key path.
         */
        private String lockedKeyPath;

        /**
         * Lock status for check lock is success.
         */
        private boolean lockSuccess;

        /**
         * Thread pool for watch and lease lock.
         */
        private ScheduledFuture<?> scheduledFuture;

        public LockData(String lockKey, Thread currentThread) {
            this.lockKey = lockKey;
            this.currentThread = currentThread;
        }

        public String getLockedKey() {
            return lockedKeyPath == null ? lockKey : lockedKeyPath;
        }
    }
}
