package com.github.yizzuide.milkomeda.demo.light.config;

import com.github.yizzuide.milkomeda.demo.light.pojo.Order;
import com.github.yizzuide.milkomeda.light.Cache;
import com.github.yizzuide.milkomeda.light.LightCache;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * LightConfig
 *
 * @author yizzuide
 * Create at 2019/07/01 17:06
 */
@Configuration
public class LightConfig {

    @Bean("lightCache")
    public Cache<String, Order> lightCache() {
        LightCache<String, Order> lightCache = new LightCache<>();
        // 设置一级缓存个数，默认为64
//        lightCache.setL1MaxCount(64);
        // 设置一级缓存超出后丢弃的百分比，默认为0.1
        lightCache.setL1DiscardPercent(.3F);
        // 设置丢弃策略，默认为HotDiscard（丢弃低频热点），可选TimelineDiscard（丢弃旧时间线数据）
//        lightCache.setDiscardStrategy(new TimelineDiscard<>());
        // 设置只存储到一级缓存，默认为false
//        lightCache.setOnlyCacheL1(false);
        return lightCache;
    }

}
