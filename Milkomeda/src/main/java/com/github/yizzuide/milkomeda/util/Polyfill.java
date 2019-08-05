package com.github.yizzuide.milkomeda.util;

import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * Polyfill
 * 兼容处理
 *
 * @author yizzuide
 * Create at 2019/07/25 18:23
 */
public class Polyfill {
    /**
     * 在redisTemplate.delete函数上兼容1.5.x.RELEASE 和 2.x.x.RELEASE版本
     * @param redisTemplate RedisTemplate
     * @param key           缓存键
     * @return  删除是否成功
     */
    public static Boolean redisDelete(RedisTemplate redisTemplate, final String key) {
        Object obj = redisTemplate.execute((RedisCallback<Object>) connection -> {
            StringRedisSerializer serializer = new StringRedisSerializer();
            Long success = connection.del(serializer.serialize(key));
            connection.close();
            return success;
        });
        long isSuccess = obj != null ? (Long)obj : 0;
        return isSuccess > 0;
    }
}
