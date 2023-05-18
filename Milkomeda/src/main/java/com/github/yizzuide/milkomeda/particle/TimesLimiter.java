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

import com.github.yizzuide.milkomeda.universe.extend.loader.LuaLoader;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.core.script.RedisScript;

import java.io.Serializable;
import java.time.Duration;
import java.util.Collections;

/**
 * TimesLimiter
 * 调用次数限制器
 *
 * @author yizzuide
 * @since 1.5.2
 * @version 3.12.10
 * <br>
 * Create at 2019/05/30 17:32
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class TimesLimiter extends LimitHandler implements LuaLoader {
    /**
     * 限制时间类型
     */
    @Setter
    private TimesType timesType;

    /**
     * 限制次数
     */
    @Getter
    @Setter
    private Long limitTimes;

    /**
     * lua脚本
     */
    private String luaScript;

    // 装饰后缀
    private static final String POSTFIX = ":times";

    public TimesLimiter() { }

    /**
     * 构造限制配置
     * @param timesType     限制时间类型
     * @param limitTimes    限制次数
     */
    public TimesLimiter(TimesType timesType, Long limitTimes) {
        this.timesType = timesType;
        this.limitTimes = limitTimes;
    }

    @Override
    public <R> R limit(String key, Process<R> process) throws Throwable {
        String decoratedKey = key + POSTFIX;
        RedisTemplate<String, Serializable> redisTemplate = getJsonRedisTemplate();
        long expireSeconds;
        switch (timesType) {
            case SEC:
                expireSeconds = 1;
                break;
            case MIN:
                expireSeconds = Duration.ofMinutes(1).getSeconds();
                break;
            case HOUR:
                expireSeconds = Duration.ofHours(1).getSeconds();
                break;
            case DAY:
                expireSeconds = Duration.ofDays(1).getSeconds();
                break;
            default:
                throw new IllegalStateException("Unexpected value: " + timesType);
        }
        RedisScript<Long> redisScript = new DefaultRedisScript<>(luaScript, Long.class);
        Long times = redisTemplate.execute(redisScript, Collections.singletonList(decoratedKey), limitTimes, expireSeconds);
        assert times != null;
        // 判断是否超过次数
        boolean isOver = times > limitTimes;
        Particle particle = new Particle(this.getClass(), isOver, times);
        return next(particle, key, process);
    }

    @Override
    public String filename() {
        return "particle_times_limiter.lua";
    }
}
