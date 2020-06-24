package com.github.yizzuide.milkomeda.particle;

import com.github.yizzuide.milkomeda.universe.polyfill.RedisPolyfill;
import com.github.yizzuide.milkomeda.util.RedisUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;

/**
 * IdempotentLimiter
 * 幂等、去重限制器
 * 同一个标识的key不能重复调用业务处理方法，相同的调用可作幂等返回处理
 *
 * @author yizzuide
 * @since 1.5.0
 * @version 3.9.0
 * Create at 2019/05/30 13:49
 */
@Slf4j
public class IdempotentLimiter extends LimitHandler {

    // 装饰后缀
    private static final String POSTFIX = ":repeat";

    @Override
    public <R> R limit(String key, long expire, Process<R> process) throws Throwable {
        String decoratedKey = key + POSTFIX;
        StringRedisTemplate redisTemplate = getRedisTemplate();
        Boolean hasObtainLock = RedisUtil.setIfAbsent(decoratedKey, expire, redisTemplate);
        assert hasObtainLock != null;
        Particle particle = new Particle(this.getClass(), !hasObtainLock, hasObtainLock ? null : "1");
        try {
            return next(particle, key, expire, process);
        } finally {
            // 只有第一次设置key的线程有权删除这个key
            if (hasObtainLock) {
                RedisPolyfill.redisDelete(redisTemplate, decoratedKey);
            }
        }
    }
}
