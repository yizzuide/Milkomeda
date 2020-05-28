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
        Long phase = redisTemplate.execute(redisScript, Collections.singletonList("moon:lhp-" + key), prototype.getPhaseNames().size());
        assert phase != null;
        return prototype.getPhaseNames().get(Math.toIntExact(phase));
    }

    @Override
    public String loadLuaScript() throws IOException {
        return IOUtils.loadLua("/META-INF/scripts", "moon_periodic.lua");
    }
}
