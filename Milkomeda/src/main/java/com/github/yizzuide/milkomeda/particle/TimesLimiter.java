package com.github.yizzuide.milkomeda.particle;

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
 * @version 3.9.0
 * Create at 2019/05/30 17:32
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class TimesLimiter extends LimitHandler {
    /**
     * 限制时间类型
     */
    private TimesType timesType;

    /**
     * 限制次数
     */
    @Getter
    @Setter
    private Long limitTimes;

    // 装饰后缀
    private static final String POSTFIX = ":times";

    // lua 脚本
    private static String luaScript;

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

    /**
     * 次数限制方法
     * @param key       键
     * @param process   处理方法回调
     * @param <R>       返回数据类型
     * @return          返回回调里的结果
     * @throws Throwable 可抛出异常
     */
    public <R> R limit(String key, Process<R> process) throws Throwable {
        return limit(key, 0, process);
    }

    @Override
    public <R> R limit(String key, long expire, Process<R> process) throws Throwable {
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
        return next(particle, key, expire, process);
    }

    static void setLuaScript(String luaScript) {
        TimesLimiter.luaScript = luaScript;
    }
}
