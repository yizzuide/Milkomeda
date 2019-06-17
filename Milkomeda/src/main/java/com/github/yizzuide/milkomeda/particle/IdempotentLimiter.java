package com.github.yizzuide.milkomeda.particle;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.StringRedisTemplate;

/**
 * IdempotentLimiter
 * 幂等、去重限制器
 * 同一个标识的key不能重复调用业务处理方法，相同的调用可作幂等返回处理
 *
 * @author yizzuide
 * @since 1.5.0
 * Create at 2019/05/30 13:49
 */
@Slf4j
public class IdempotentLimiter extends LimitHandler {

    @Autowired
    private StringRedisTemplate redisTemplate;

    // 装饰后缀
    private static final String POSTFIX = ":repeat";

    @Override
    public <R> R limit(String key, long expire, Process<R> process) throws Throwable {
        String decoratedKey = key + POSTFIX;
        val isAbsent = redisTemplate.execute((RedisCallback<Boolean>) connection -> {
            byte[] keyBytes = redisTemplate.getStringSerializer().serialize(decoratedKey);
            byte[] valueBytes = redisTemplate.getStringSerializer().serialize(("1"));
            // 将设置key
            val success = connection.setNX(keyBytes, valueBytes);
            assert success != null;
            if (success) {
                connection.expire(keyBytes, expire);
            }
            return success;
        });
        assert isAbsent != null;
        Particle particle = new Particle(this.getClass(), !isAbsent, isAbsent ? null : "1");
        try {
            // 如果未被限制，且有下一个处理器
            if (!particle.isLimited() && null != getNext()) {
                return getNext().limit(key, expire, process);
            }
            return process.apply(particle);
        } finally {
            // 只有第一次设置key的线程有权删除这个key
            if (isAbsent) redisTemplate.delete(decoratedKey);
        }
    }
}
