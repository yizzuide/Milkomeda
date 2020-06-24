package com.github.yizzuide.milkomeda.particle;

import com.github.yizzuide.milkomeda.universe.context.ApplicationContextHolder;
import lombok.Data;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.io.Serializable;

/**
 * LimitHandler
 * 限制处理器链
 *
 * @author yizzuide
 * @since 1.5.0
 * @version 3.9.0
 * Create at 2019/05/31 01:22
 */
@Data
public abstract class LimitHandler implements Limiter {
    /**
     * 拦截链串，用于记录链条数据
     */
    protected LimitHandler next;

    private StringRedisTemplate redisTemplate;

    private RedisTemplate<String, Serializable> jsonRedisTemplate;

    protected final StringRedisTemplate getRedisTemplate() {
        if (redisTemplate == null) {
            redisTemplate = ApplicationContextHolder.get().getBean(StringRedisTemplate.class);
        }
        return redisTemplate;
    }

    @SuppressWarnings("unchecked")
    protected final RedisTemplate<String, Serializable> getJsonRedisTemplate() {
        if (jsonRedisTemplate == null) {
            jsonRedisTemplate = ApplicationContextHolder.get().getBean("jsonRedisTemplate", RedisTemplate.class);
        }
        return jsonRedisTemplate;
    }

    /**
     * 限制器链调用
     * @param particle  状态数据
     * @param key       键
     * @param expire    过期时间
     * @param process   处理方法
     * @param <R>       返回类型
     * @return R
     * @throws Throwable 可抛出异常
     */
    protected <R> R next(Particle particle, String key, long expire, Process<R> process) throws Throwable {
        // 如果未被限制，且有下一个处理器
        if (!particle.isLimited() && null != getNext()) {
            return getNext().limit(key, expire, process);
        }
        return process.apply(particle);
    }
}
