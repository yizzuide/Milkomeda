package com.github.yizzuide.milkomeda.particle;

/**
 * Process
 * 业务处理接口
 *
 * @author yizzuide
 * @since 1.5.0
 * Create at 2019/05/30 16:57
 */
@FunctionalInterface
public interface Process<R> {
    /**
     * 处理方法
     * @param particle Particle
     * @return R
     * @throws Throwable 可抛出异常
     */
    R apply(Particle particle) throws Throwable;
}
