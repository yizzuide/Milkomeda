package com.github.yizzuide.milkomeda.light;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * LightProperties
 *
 * @author yizzuide
 * @version 1.17.0
 * Create at 2019/12/03 16:24
 */
@Data
@ConfigurationProperties(prefix = "milkomeda.light")
public class LightProperties {
    /** 一级缓存个数（默认64) */
    private int l1MaxCount = 64;

    /** 设置一级缓存超出后丢弃的百分比 (默认0.1）*/
    private float L1DiscardPercent = 0.1f;

    /** 一级缓存丢弃策略（默认为Hot）*/
    private LightDiscardStrategy strategy = LightDiscardStrategy.HOT;

    /** 一级缓存自定义丢弃策略实现类 （设置 LightDiscardStrategy.CUSTOM 时有效）*/
    private Class<Discard> strategyClass;

    /** 只缓存在一级缓存上 */
    private boolean onlyCacheL1 = false;

    /** 二级缓存过期时间（单位s，默认不过期）*/
    private long l2Expire = -1L;
}
