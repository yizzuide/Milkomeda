package com.github.yizzuide.milkomeda.particle;

import com.github.yizzuide.milkomeda.universe.context.ApplicationContextHolder;
import lombok.Data;
import org.springframework.data.redis.core.StringRedisTemplate;

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

    StringRedisTemplate getRedisTemplate() {
        if (redisTemplate == null) {
            redisTemplate = ApplicationContextHolder.get().getBean(StringRedisTemplate.class);
        }
        return redisTemplate;
    }
}
