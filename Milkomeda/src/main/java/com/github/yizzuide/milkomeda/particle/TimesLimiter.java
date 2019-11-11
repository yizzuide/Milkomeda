package com.github.yizzuide.milkomeda.particle;

import com.github.yizzuide.milkomeda.util.RedisUtil;
import lombok.Getter;
import lombok.Setter;
import org.joda.time.DateTime;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.Date;

/**
 * TimesLimiter
 * 调用次数限制器
 *
 * @author yizzuide
 * @since 1.5.2
 * @version 1.14.0
 * Create at 2019/05/30 17:32
 */
public class TimesLimiter extends LimitHandler {
    /**
     * 限制时间类型
     */
    @Getter @Setter
    private TimesType timesType;

    /**
     * 限制次数
     */
    @Getter @Setter
    private Long limitTimes;

    // 装饰后缀
    private static final String POSTFIX = ":times";

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
        StringRedisTemplate redisTemplate = getRedisTemplate();
        Date date;
        switch (timesType) {
            case SEC:
                date = DateTime.now().plusSeconds(1).toDate();
                break;
            case MIN:
                date = DateTime.now().plusMinutes(1).toDate();
                break;
            case HOUR:
                date = DateTime.now().plusHours(1).toDate();
                break;
            case DAY:
                date = DateTime.now().plusDays(1).toDate();
                break;
            default:
                throw new IllegalStateException("Unexpected value: " + timesType);
        }
        long times = RedisUtil.incr(decoratedKey, 1, date, redisTemplate);
        // 判断是否超过次数
        boolean isOver = times > limitTimes;
        Particle particle = new Particle(this.getClass(), isOver, times);
        // 如果未被限制，且有下一个处理器
        if (!particle.isLimited() && null != getNext()) {
            return getNext().limit(key, expire, process);
        }
        return process.apply(particle);
    }
}
