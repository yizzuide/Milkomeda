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
 * @version 3.12.10
 * Create at 2019/05/31 01:22
 */
@Data
public abstract class LimitHandler implements Limiter {
    /**
     * 过期时间
     * @since 3.12.10
     */
    protected Long expire;
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
     * @param process   业务处理方法
     * @param <R>       返回类型
     * @return R
     * @throws Throwable 可抛出异常
     */
    protected <R> R next(Particle particle, String key, Process<R> process) throws Throwable {
        // 如果未被限制，且有下一个处理器
        if (!particle.isLimited() && null != getNext()) {
            return getNext().limit(key, process);
        }
        return process.apply(particle);
    }
}
