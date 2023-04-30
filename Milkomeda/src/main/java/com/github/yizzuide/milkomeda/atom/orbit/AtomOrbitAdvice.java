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

package com.github.yizzuide.milkomeda.atom.orbit;

import com.github.yizzuide.milkomeda.atom.*;
import com.github.yizzuide.milkomeda.orbit.OrbitAdvice;
import com.github.yizzuide.milkomeda.orbit.OrbitInvocation;
import com.github.yizzuide.milkomeda.universe.engine.el.ELContext;
import com.github.yizzuide.milkomeda.util.ReflectUtil;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.AnnotationUtils;

import java.time.Duration;

/**
 * This advice listen on method which annotated {@link AtomLock} has invoked.
 *
 * @since 3.15.0
 * @author yizzuide
 * <br>
 * Create at 2023/01/27 19:38
 */
@SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
@Slf4j
@Data
public class AtomOrbitAdvice implements OrbitAdvice {

    @Autowired
    private Atom atom;

    @Override
    public Object invoke(OrbitInvocation invocation) throws Throwable {
        ProceedingJoinPoint joinPoint = invocation.getPjp();
        AtomLock atomLock = AnnotationUtils.findAnnotation(invocation.getMethod(), AtomLock.class);
        assert atomLock != null;
        String keyPath = ELContext.getValue(joinPoint, atomLock.key());
        Object lock = null;
        boolean isLocked = false;
        try {
            if (atomLock.waitTime() > 0) {
                AtomLockInfo lockInfo = this.getAtom().tryLock(keyPath, atomLock.type(), atomLock.readOnly());
                isLocked = lockInfo.isLocked();
                lock = lockInfo.getLock();
                if (!isLocked) {
                    lockInfo = this.getAtom().tryLock(keyPath, Duration.ofMillis(atomLock.waitTime()), Duration.ofMillis(atomLock.leaseTime()), atomLock.type(), atomLock.readOnly());
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
            AtomLockInfo lockInfo = this.getAtom().lock(keyPath, Duration.ofMillis(atomLock.leaseTime()), atomLock.type(), atomLock.readOnly());
            lock = lockInfo.getLock();
            isLocked = lockInfo.isLocked();
            return joinPoint.proceed();
        } catch (InterruptedException e) {
            log.error("Atom try lock error with msg: {}", e.getMessage(), e);
        } finally {
            // 只有加锁的线程需要解锁
            if (isLocked && lock != null && this.getAtom().isLocked(lock)) {
                this.getAtom().unlock(lock);
            }
        }
        // unreachable code!
        return null;
    }
}
