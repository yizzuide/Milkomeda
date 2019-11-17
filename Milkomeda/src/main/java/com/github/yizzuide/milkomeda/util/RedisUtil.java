package com.github.yizzuide.milkomeda.util;

import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.data.redis.support.atomic.RedisAtomicLong;

import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * RedisUtil
 *
 * @author yizzuide
 * @since 1.14.0
 * Create at 2019/11/11 14:07
 */
public class RedisUtil {
    /**
     * SetNX
     * @param key           键
     * @param liveTime      过期时间
     * @param redisTemplate RedisTemplate
     * @return 返回true，表示存在
     */
    public static Boolean setIfAbsent(String key, long liveTime, RedisTemplate<String, String> redisTemplate) {
        return redisTemplate.opsForValue().setIfAbsent(key, "1", liveTime, TimeUnit.SECONDS);
    }
    /**
     * 自增键
     * @param key           键
     * @param delta         自增量
     * @param expireDate    过期时间
     * @param redisTemplate RedisTemplate
     * @return 当前值
     */
    public static Long incr(String key, long delta, Date expireDate, RedisTemplate redisTemplate) {
        assert redisTemplate.getConnectionFactory() != null;
        RedisAtomicLong ral = new RedisAtomicLong(key, redisTemplate.getConnectionFactory());
        long increment = ral.addAndGet(delta);
        if (expireDate != null) {
            ral.expireAt(expireDate);
        }
        return increment;
    }

    /**
     * 自增键
     * @param key           键
     * @param delta         自增量
     * @param liveTime      过期时间（单位：sec)
     * @param redisTemplate RedisTemplate
     * @return 当前值
     */
    public static Long incr(String key, long delta, long liveTime, RedisTemplate redisTemplate) {
        assert redisTemplate.getConnectionFactory() != null;
        RedisAtomicLong ral = new RedisAtomicLong(key, redisTemplate.getConnectionFactory());
        long increment = ral.addAndGet(delta);
        if (liveTime != 0) {
            ral.expire(liveTime, TimeUnit.SECONDS);
        }
        return increment;
    }

    /**
     * 批量操作
     * @param runnable      业务体
     * @param redisTemplate RedisTemplate
     */
    public static void batchOps(Runnable runnable, RedisTemplate<String, String> redisTemplate) {
        redisTemplate.executePipelined(new SessionCallback<Object>() {
            @Override
            public <K, V> Object execute(RedisOperations<K, V> operations) throws DataAccessException {
                runnable.run();
                return null;
            }
        });
    }
}
