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

package com.github.yizzuide.milkomeda.moon;

import com.github.yizzuide.milkomeda.util.IOUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.core.script.RedisScript;

import java.io.IOException;
import java.io.Serializable;
import java.util.Collections;
import java.util.concurrent.locks.ReentrantLock;

/**
 * PeriodicMoonStrategy
 * 周期性分配
 *
 * @author yizzuide
 * @since 2.6.0
 * @version 3.7.0
 * Create at 2020/03/13 21:20
 */
public class PeriodicMoonStrategy extends AbstractLuaMoonStrategy {
    /**
     * 分布式key前缀
     */
    private static final String PREFIX = "moon:periodic-";
    // 并发指针锁
    private final ReentrantLock reentrantLock = new ReentrantLock(false);

    @Override
    public <T> T getCurrentPhase(Moon<T> moon) {
        this.reentrantLock.lock();
        T data = moon.getPointer().getData();
        moon.setPointer(moon.getPointer().getNext());
        this.reentrantLock.unlock();
        return data;
    }

    @Override
    public <T> T getPhase(String key, Integer p, Moon<T> prototype) {
        // 先获取链头
        MoonNode<T> next = prototype.getHeader();
        // 如果不是指向头，向后拔动
        if (p != 0) {
            for (int i = 0; i < p; i++) {
                next = next.getNext();
            }
        }
        return next.getData();
    }

    @Override
    public LeftHandPointer pluck(Moon<?> moon, LeftHandPointer leftHandPointer) {
        int p = leftHandPointer.getCurrent();
        // 保持指针下标在所有月相范围内
        p = (p + 1) % moon.getLen();
        leftHandPointer.setCurrent(p);
        return leftHandPointer;
    }

    @Override
    public <T> T getPhaseFast(String key, Moon<T> prototype) {
        RedisTemplate<String, Serializable> redisTemplate = getJsonRedisTemplate();
        RedisScript<Long> redisScript = new DefaultRedisScript<>(getLuaScript(), Long.class);
        Long phase = redisTemplate.execute(redisScript, Collections.singletonList(PREFIX + key), prototype.getPhaseNames().size());
        assert phase != null;
        return prototype.getPhaseNames().get(Math.toIntExact(phase));
    }

    @Override
    public String loadLuaScript() throws IOException {
        return IOUtils.loadLua("/META-INF/scripts", "moon_periodic.lua");
    }
}
