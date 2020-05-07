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
 * @version 1.14.0
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
}
