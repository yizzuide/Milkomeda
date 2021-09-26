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

import com.github.yizzuide.milkomeda.universe.el.ELContext;
import com.github.yizzuide.milkomeda.util.ReflectUtil;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;

import java.time.Duration;

/**
 * AtomLockAspect
 *
 * @author yizzuide
 * @since 3.3.0
 * @version 3.7.0
 * Create at 2020/04/30 16:26
 */
@Slf4j
@Order
@Aspect
public class AtomLockAspect {

    @Autowired
    private Atom atom;

    @Around("@annotation(atomLock) && execution(public * *(..))")
    public Object pointCut(ProceedingJoinPoint joinPoint, AtomLock atomLock) throws Throwable {
        String keyPath = ELContext.getValue(joinPoint, atomLock.key());
        Object lock = null;
        boolean isLocked = false;
        try {
            if (atomLock.waitTime() > 0) {
                AtomLockInfo lockInfo = atom.tryLock(keyPath, atomLock.type(), atomLock.readOnly());
                isLocked = lockInfo.isLocked();
                lock = lockInfo.getLock();
                if (!isLocked) {
                    lockInfo = atom.tryLock(keyPath, Duration.ofMillis(atomLock.waitTime()), Duration.ofMillis(atomLock.leaseTime()), atomLock.type(), atomLock.readOnly());
                    isLocked = lockInfo.isLocked();
                    lock = lockInfo.getLock();
                }
                if (isLocked) {
                    return joinPoint.proceed();
                }
                if (atomLock.waitTimeoutType() == AtomLockWaitTimeoutType.THROW_EXCEPTION) {
                    throw new AtomLockWaitTimeoutException();
                } else if (atomLock.waitTimeoutType() == AtomLockWaitTimeoutType.FALLBACK) {
                    String fallback = atomLock.fallback();
                    return ELContext.getActualValue(joinPoint, fallback, ReflectUtil.getMethodReturnType(joinPoint));
                }
                // here is AtomLockWaitTimeoutType.WAIT_INFINITE
            }
            AtomLockInfo lockInfo = atom.lock(keyPath, Duration.ofMillis(atomLock.leaseTime()), atomLock.type(), atomLock.readOnly());
            lock = lockInfo.getLock();
            isLocked = lockInfo.isLocked();
            return joinPoint.proceed();
        } catch (InterruptedException e) {
            log.error("Atom try lock error with msg: {}", e.getMessage(), e);
        } finally {
            // 只有加锁的线程需要解锁
            if (isLocked && lock != null && atom.isLocked(lock)) {
                atom.unlock(lock);
            }
        }
        // unreachable code!
        return null;
    }

}
