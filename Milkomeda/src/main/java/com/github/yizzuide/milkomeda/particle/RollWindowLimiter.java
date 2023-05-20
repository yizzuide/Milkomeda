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

import com.github.yizzuide.milkomeda.universe.extend.loader.LuaLoader;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.core.script.RedisScript;

import java.util.Collections;

/**
 * Roll window limiter provides more precise time limit relative to {@link TimesLimiter}.
 *
 * @since 3.15.0
 * @author yizzuide
 * <br>
 * Create at 2023/05/19 02:57
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class RollWindowLimiter extends LimitHandler implements LuaLoader {

    /**
     * Decorated postfix for limiter key.
     */
    private static final String POSTFIX = ":roll";

    /**
     * Max limit count.
     */
    private Long limitTimes = 1L;

    /**
     * Lua script list.
     */
    private String[] luaScripts;


    @Override
    public <R> R limit(String key, Process<R> process) throws Throwable {
        String decoratedKey = key + POSTFIX;
        RedisScript<Long> redisScript = new DefaultRedisScript<>(luaScripts[0], Long.class);
        long currentTimeMillis = System.currentTimeMillis();
        Long times =  getJsonRedisTemplate().execute(redisScript, Collections.singletonList(decoratedKey), expire, currentTimeMillis, limitTimes);
        assert times != null;
        // first time: times == 0, let it go!
        boolean isOver = times >= limitTimes;
        Particle particle = new Particle(this.getClass(), isOver, times);
        return next(particle, key, process);
    }

    @Override
    public String[] luaFilenames() {
        return new String[]{"particle_rollWindow_limiter.lua"};
    }
}
