/*
 * Copyright (c) 2021 yizzuide All rights Reserved.
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.github.yizzuide.milkomeda.util;

import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.data.redis.support.atomic.RedisAtomicLong;
import org.springframework.lang.NonNull;

import java.util.Date;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * RedisUtil
 *
 * @author yizzuide
 * @since 1.14.0
 * @version 3.9.0
 * <br>
 * Create at 2019/11/11 14:07
 */
public class RedisUtil {
    /**
     * SetNX
     * @param key           键
     * @param liveTime      过期时间
     * @param redisTemplate RedisTemplate
     * @return 返回true，表示加锁成功
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
     * @param callback      业务回调
     * @param redisTemplate RedisTemplate
     */
    @SuppressWarnings("unchecked")
    public static void batchOps(Consumer<RedisOperations<String, String>> callback, RedisTemplate<String, String> redisTemplate) {
        redisTemplate.executePipelined(new SessionCallback<Object>() {
            @Override
            public <K, V> Object execute(@NonNull RedisOperations<K, V> operations) throws DataAccessException {
                callback.accept((RedisOperations<String, String>) operations);
                return null;
            }
        });
    }

    /**
     * 批量操作
     * @param callback      业务回调
     * @param redisTemplate RedisTemplate
     */
    public static void batchConn(Consumer<RedisConnection> callback, RedisTemplate<String, String> redisTemplate) {
        redisTemplate.executePipelined((RedisCallback<Long>) (connection) -> {
            callback.accept(connection);
            return null;
        });
    }
}
