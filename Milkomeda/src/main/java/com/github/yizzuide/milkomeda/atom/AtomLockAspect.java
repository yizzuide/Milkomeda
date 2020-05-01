package com.github.yizzuide.milkomeda.atom;

import com.github.yizzuide.milkomeda.universe.el.ELContext;
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
        boolean needUnlock = true;
        try {
            if (atomLock.waitTime() > 0) {
                boolean tryAcquireOnce = atom.tryLock(keyPath, atomLock.type(), atomLock.readOnly());
                if (tryAcquireOnce || atom.tryLock(keyPath,
                        Duration.ofMillis(atomLock.waitTime()), Duration.ofMillis(atomLock.leaseTime()), atomLock.type(), atomLock.readOnly())) {
                    return joinPoint.proceed();
                }
                needUnlock = false;
                throw new AtomLockWaitTimeoutException();
            }
            atom.lock(keyPath, Duration.ofMillis(atomLock.leaseTime()), atomLock.type(), atomLock.readOnly());
            return joinPoint.proceed();
        } catch (InterruptedException e) {
            log.error("Atom try lock error with msg: {}", e.getMessage(), e);
        } finally {
            if (needUnlock) {
                try {
                    atom.unlock(keyPath);
                } catch (IllegalMonitorStateException ignore) {
                    // Ignore unlock fail which auto unlocked!
                }
            }
        }
        // unreachable code!
        return null;
    }

}
