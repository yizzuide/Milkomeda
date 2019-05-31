package com.github.yizzuide.milkomeda.particle;

import com.github.yizzuide.milkomeda.universe.context.ApplicationContextHolder;
import com.github.yizzuide.milkomeda.util.ReflectUtil;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.core.annotation.Order;
import org.springframework.util.StringUtils;

/**
 * ParticleAspect
 *
 * @author yizzuide
 * @since 1.5.0
 * Create at 2019/05/30 22:29
 */
@Slf4j
@Aspect
@Order(8)
public class ParticleAspect {

    // 切入点
    @Pointcut("@annotation(com.github.yizzuide.milkomeda.particle.Limit)")
    public void particlePointCut() {}

    @Around("particlePointCut()")
    public Object doAround(ProceedingJoinPoint joinPoint) throws Throwable {
        val particleLimiter = ReflectUtil.getAnnotation(joinPoint, Limit.class);
        String beanName = particleLimiter.limiterBeanName();
        String prefix = particleLimiter.name();
        String key = particleLimiter.key();
        if (StringUtils.isEmpty(key)) {
            throw new IllegalArgumentException("You must set key for use Limit.");
        }
        // 解析表达式
        key = ReflectUtil.extractValue(joinPoint, key);
        long expire = particleLimiter.expire();
        Limiter limiter = !StringUtils.isEmpty(beanName) ? ApplicationContextHolder.get().getBean(beanName, Limiter.class)
                : ApplicationContextHolder.get().getBean(particleLimiter.limiterBeanClass());
        return limiter.limit(prefix + ":" + key, expire, (particle -> joinPoint.proceed(injectParam(joinPoint, particle, true))));
    }

    /**
     * 注入参数
     * @param joinPoint 连接点
     * @param particle  Particle
     * @param check     检查参数列表
     * @return  注入后的参数列表
     */
    private Object[] injectParam(JoinPoint joinPoint, Particle particle, boolean check) {
        Object[] args = joinPoint.getArgs();
        int len = args.length;
        boolean flag = false;
        for (int i = 0; i < len; i++) {
            if (args[i] instanceof Particle) {
                args[i] = particle;
                flag = true;
                return args;
            }
        }
        if (check && !flag) {
            throw new IllegalArgumentException("You must add Particle parameter on method " +
                    joinPoint.getSignature().getName() + " before use Limit.");
        }
        return args;
    }
}
