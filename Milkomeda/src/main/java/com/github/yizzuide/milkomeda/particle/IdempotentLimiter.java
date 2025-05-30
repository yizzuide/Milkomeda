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

package com.github.yizzuide.milkomeda.particle;

import com.github.yizzuide.milkomeda.util.RedisUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.UUID;

/**
 * IdempotentLimiter
 * 幂等、去重限制器
 * 同一个标识的key不能重复调用业务处理方法，相同的调用可作幂等返回处理
 *
 * @author yizzuide
 * @since 1.5.0
 * @version 3.12.10
 * <br>
 * Create at 2019/05/30 13:49
 */
@Slf4j
public class IdempotentLimiter extends LimitHandler {

    // 装饰后缀
    private static final String POSTFIX = ":repeat";

    @Override
    public <R> R limit(String key, Process<R> process) throws Throwable {
        String decoratedKey = key + POSTFIX;
        StringRedisTemplate redisTemplate = getRedisTemplate();
        String clientId = UUID.randomUUID().toString();
        Boolean hasObtainLock = RedisUtil.setIfAbsent(decoratedKey, clientId, expire, redisTemplate);
        assert hasObtainLock != null;
        Particle particle = new Particle(this.getClass(), !hasObtainLock, hasObtainLock ? null : "1");
        try {
            return next(particle, key, process);
        } finally {
            if (hasObtainLock) {
                // 删除Lock
                RedisUtil.unlock(decoratedKey, clientId, redisTemplate);
            }
        }
    }
}
