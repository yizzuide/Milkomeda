package com.github.yizzuide.milkomeda.light;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.convert.DurationUnit;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;

/**
 * LightProperties
 *
 * @author yizzuide
 * @since 1.17.0
 * @version 3.0.0
 * Create at 2019/12/03 16:24
 */
@Data
@ConfigurationProperties(prefix = "milkomeda.light")
public class LightProperties {
    /** 一级缓存个数（不适用于LightDiscardStrategy.LazyExpire） */
    private int l1MaxCount = 64;

    /** 设置一级缓存超出后丢弃的百分比 （不适用于LightDiscardStrategy.LazyExpire）*/
    private float l1DiscardPercent = 0.1f;

    /**  一级缓存过期时间（单位s，只适用于LightDiscardStrategy.LazyExpire） */
    @DurationUnit(ChronoUnit.SECONDS)
    private Duration l1Expire = Duration.ofSeconds(-1);

    /** 一级缓存丢弃策略（默认为Hot）*/
    private LightDiscardStrategy strategy = LightDiscardStrategy.HOT;

    /** 一级缓存自定义丢弃策略实现类 （设置 LightDiscardStrategy.CUSTOM 时有效）*/
    private Class<Discard> strategyClass;

    /** 只缓存在一级缓存上 */
    private boolean onlyCacheL1 = false;

    /** 二级缓存过期时间（单位s，默认不过期）*/
    @DurationUnit(ChronoUnit.SECONDS)
    private Duration l2Expire = Duration.ofSeconds(-1);

    /** 只缓存在二级缓存上 */
    private boolean onlyCacheL2 = false;
    /**
     * 自定义实例名配置
     */
    private Map<String, LightProperties> instances = new HashMap<>();
}
