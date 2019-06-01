package com.github.yizzuide.milkomeda.particle;

/**
 * Limiter
 * 限制器策略接口
 *
 * @author yizzuide
 * @since 1.5.0
 * Create at 2019/05/30 13:41
 */
public interface Limiter {

    /**
     * 检测限制情况，结果通过ParticleProcess参数 Particle.isLimited() 判断
     * @param key       键
     * @param expire    过期时间
     * @param process   处理方法
     * @return R
     * @throws Throwable 可抛出异常
     */
    <R> R limit(String key, long expire, Process<R> process) throws Throwable;
}
