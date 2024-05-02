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

package com.github.yizzuide.milkomeda.universe.polyfill;

import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * Polyfill
 * 兼容处理
 *
 * @author yizzuide
 * @since 1.5.0
 * <br>
 * Create at 2019/07/25 18:23
 */
public class RedisPolyfill {
    /**
     * 在redisTemplate.delete函数上兼容1.5.x.RELEASE 和 2.x.x.RELEASE版本
     * @param redisTemplate RedisTemplate
     * @param key           缓存键
     * @return  删除是否成功
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
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
