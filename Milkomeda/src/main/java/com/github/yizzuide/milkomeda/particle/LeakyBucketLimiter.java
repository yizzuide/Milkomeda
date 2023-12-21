/*
 * Copyright (c) 2023 yizzuide All rights Reserved.
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

import com.github.yizzuide.milkomeda.universe.extend.env.Environment;
import com.github.yizzuide.milkomeda.universe.extend.loader.LuaLoader;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.core.script.RedisScript;

import java.util.Collections;

/**
 * The leaky bucket limiter used fixed size bucket, fixed rate outflow, and receive requests at any rate,
 * but reject all requests if the bucket is full.
 *
 * @since 3.15.0
 * @version 3.15.3
 * @author yizzuide
 * Create at 2023/05/21 18:20
 */
@Slf4j
@EqualsAndHashCode(callSuper = true)
@Data
public class LeakyBucketLimiter extends LimitHandler implements LuaLoader {

    /**
     * Decorated postfix for a limiter key.
     */
    private static final String POSTFIX = ":leaky_bucket";

    /**
     * Bucket size.
     */
    private long capacity;

    /**
     * loss water count per second (handle request per second).
     */
    private long rate;

    /**
     * Lua script list.
     */
    private String[] luaScripts;

    @Override
    public <R> R limit(String key, Process<R> process) throws Throwable {
        String limiterKey = key + POSTFIX;
        RedisScript<Long> redisScript = new DefaultRedisScript<>(luaScripts[0], Long.class);
        Long waterCount = getJsonRedisTemplate().execute(redisScript, Collections.singletonList(limiterKey), getCapacity(), getRate(), System.currentTimeMillis());
        if (Environment.isShowLog()) {
            log.info("particle drop water from bucket, leave water count: {}", waterCount);
        }
        // -1 is means bucket is fulled.
        boolean isOver = waterCount == null || waterCount == -1;
        Particle particle = new Particle(this.getClass(), isOver, null);
        return next(particle, key, process);
    }

    @Override
    public String[] luaFilenames() {
        return new String[] {"particle_leakyBucket_limiter.lua"};
    }
}
