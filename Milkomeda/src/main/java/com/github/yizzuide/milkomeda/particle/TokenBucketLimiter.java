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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

import java.time.Duration;
import java.time.Instant;
import java.util.Collections;
import java.util.concurrent.CountDownLatch;

/**
 * The token bucket not be pre consumed and solved the problem of double traffic per unit time which relative to {@link TimesLimiter}.
 *
 * @since 3.15.0
 * @author yizzuide
 * <br>
 * Create at 2023/05/19 21:55
 */
@Slf4j
@EqualsAndHashCode(callSuper = true)
@Data
public class TokenBucketLimiter extends LimitHandler implements LuaLoader {

    /**
     * Decorated postfix for limiter key.
     */
    private static final String POSTFIX = ":token_bucket";

    /**
     * Synchronized State for JMM.
     */
    private volatile int startState = 0;

    /**
     * Sync Lock for wait task put tokens into bucket.
     */
    private CountDownLatch countDownLatch = new CountDownLatch(1);

    /**
     * Task pool scheduler.
     */
    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    @Autowired
    private ThreadPoolTaskScheduler taskScheduler;

    /**
     * Lua script list.
     */
    private String[] luaScripts;

    /**
     * Current limiter key.
     */
    private String limiterKey;

    /**
     * Bucket size.
     */
    private int bucketCapacity;

    /**
     * Each time of put token count in bucket.
     */
    private int tokensPerTime;

    /**
     * Interval of put token in bucket (second unit).
     */
    private int interval;

    @Override
    public <R> R limit(String key, Process<R> process) throws Throwable {
        limiterKey = key + POSTFIX;
        // first time, wait tokens added in bucket.
        if (startState == 0) {
            synchronized (this) {
                if (startState == 0) {
                    startState = 1;
                    startTask();
                    countDownLatch.await();
                }
            }
        }
        RedisScript<Long> redisScript = new DefaultRedisScript<>(luaScripts[1], Long.class);
        Long tokenCount = getJsonRedisTemplate().execute(redisScript, Collections.singletonList(limiterKey));
        if (Environment.isShowLog()) {
            log.info("particle get tokens from bucket, leave token count: {}", tokenCount);
        }
        // -1 is means bucket empty
        boolean isOver = tokenCount == null || tokenCount == -1;
        Particle particle = new Particle(this.getClass(), isOver, null);
        return next(particle, key, process);
    }

    private void startTask() {
        // task for put tokens in bucket
        taskScheduler.scheduleAtFixedRate(() -> {
            if (startState == 0) {
                return;
            }
            RedisScript<Long> redisScript = new DefaultRedisScript<>(luaScripts[0], Long.class);
            long currentTimeMillis = System.currentTimeMillis();
            Long tokenCount = getJsonRedisTemplate().execute(redisScript, Collections.singletonList(limiterKey), bucketCapacity, tokensPerTime, interval, currentTimeMillis);
            if (Environment.isShowLog()) {
                log.info("particle task put tokens into bucket, current token count: {}", tokenCount);
            }
            // release lock
            if (countDownLatch != null && countDownLatch.getCount() > 0) {
                countDownLatch.countDown();
                countDownLatch = null;
            }
        }, Instant.now(), Duration.ofSeconds(interval));
    }

    @Override
    public String[] luaFilenames() {
        return new String[]{"particle_tokenBucket_limiter.lua", "particle_tokenBucket_consume_limiter.lua"};
    }

}
