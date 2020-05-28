package com.github.yizzuide.milkomeda.moon;

import com.github.yizzuide.milkomeda.universe.context.ApplicationContextHolder;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.core.script.RedisScript;

import java.util.Collections;
import java.util.concurrent.locks.ReentrantLock;

/**
 * PeriodicMoonStrategy
 * 周期性分配
 *
 * @author yizzuide
 * @since 2.6.0
 * Create at 2020/03/13 21:20
 */
public class PeriodicMoonStrategy implements MoonStrategy {

    // 并发指针锁
    private final ReentrantLock reentrantLock = new ReentrantLock(false);

    // lua脚本
    private static String luaScript;

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

    @SuppressWarnings("unchecked")
    @Override
    public <T> T getPhaseFast(String key, Moon<T> prototype) {
        RedisScript<Integer> redisScript = new DefaultRedisScript<>(luaScript, Integer.class);
        RedisTemplate<String, Object> redisTemplate = ApplicationContextHolder.get().getBean("redisTemplate", RedisTemplate.class);
        return (T) redisTemplate.execute(redisScript, Collections.singletonList("moon:lhp-" + key), prototype.getPhaseNames().size());
    }

    static void setLuaScript(String luaScript) {
        PeriodicMoonStrategy.luaScript = luaScript;
    }
}
