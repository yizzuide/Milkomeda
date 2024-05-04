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
 * @version 4.0.0
 * <br>
 * Create at 2019/12/03 16:24
 */
@Data
@ConfigurationProperties(prefix = "milkomeda.light")
public class LightProperties {
    /** 一级缓存个数（不适用于LightDiscardStrategy.LazyExpire） */
    private int l1MaxCount = 64;

    /** 设置一级缓存超出后丢弃的百分比 （不适用于LightDiscardStrategy.LazyExpire）*/
    private float l1DiscardPercent = 0.35f;

    /**  一级缓存过期时间（单位s，仅适用于LightDiscardStrategy.LazyExpire） */
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
     * 是否开启超缓存
     * @since 3.12.4
     */
    private boolean enableSuperCache = true;

    /**
     * 是否开启ThreadLocalScope
     * @since 3.13.0
     */
    private boolean enableLightThreadLocalScope = false;

    /**
     * 数据源获取使用分布式互斥锁
     * @since 4.0.0
     */
    private boolean dataGenerateMutex = false;

    /**
     * 自定义实例名配置（实例的注册方式为首次使用时）
     */
    private Map<String, LightProperties> instances = new HashMap<>();
}
