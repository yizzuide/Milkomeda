package com.github.yizzuide.milkomeda.particle;

import com.github.yizzuide.milkomeda.universe.context.ApplicationContextHolder;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.core.annotation.Order;
import org.springframework.util.StringUtils;

import static com.github.yizzuide.milkomeda.util.ReflectUtil.*;

/**
 * ParticleAspect
 *
 * @author yizzuide
 * @since 1.5.0
 * @version 3.1.0
 * Create at 2019/05/30 22:29
 */
@Slf4j
@Aspect
@Order(88)
public class ParticleAspect {

    // 切入点
    @Pointcut("@annotation(com.github.yizzuide.milkomeda.particle.Limit)")
    public void particlePointCut() {}

    @Around("particlePointCut()")
    public Object doAround(ProceedingJoinPoint joinPoint) throws Throwable {
        val limit = getAnnotation(joinPoint, Limit.class);
        String beanName = limit.limiterBeanName();
        String prefix = limit.name();
        String key = limit.key();
        if (StringUtils.isEmpty(key)) {
            throw new IllegalArgumentException("You must set key for use Limit.");
        }
        // 解析表达式
        key = extractValue(joinPoint, key);
        long expire = limit.expire();
        Limiter limiter = !StringUtils.isEmpty(beanName) ? ApplicationContextHolder.get().getBean(beanName, Limiter.class)
                : ApplicationContextHolder.get().getBean(limit.limiterBeanClass());
        String decorateKey = StringUtils.isEmpty(prefix) ? key : prefix + ":" + key;
        return limiter.limit(decorateKey, expire, (particle ->
                joinPoint.proceed(injectParam(joinPoint, particle, limit, true))));
    }
}
