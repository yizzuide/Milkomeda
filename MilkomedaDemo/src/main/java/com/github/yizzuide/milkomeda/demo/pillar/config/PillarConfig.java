package com.github.yizzuide.milkomeda.demo.pillar.config;

import com.github.yizzuide.milkomeda.demo.pillar.common.ReturnData;
import com.github.yizzuide.milkomeda.demo.pillar.pojo.PayEntity;
import com.github.yizzuide.milkomeda.light.LightCache;
import com.github.yizzuide.milkomeda.light.TimelineDiscard;
import com.github.yizzuide.milkomeda.pillar.Pillar;
import com.github.yizzuide.milkomeda.pillar.PillarExecutor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.Map;

/**
 * PillarConfig
 *
 * @author yizzuide
 * Create at 2019/05/25 21:21
 */
@Configuration
public class PillarConfig {

    @Autowired
    private List<Pillar<Map<String, String>, ReturnData>> pillars;

    @Bean
    public PillarExecutor<Map<String, String>, ReturnData> pillarExecutor() {
        PillarExecutor<Map<String, String>, ReturnData> pillarExecutor = new PillarExecutor<>();
        pillarExecutor.addPillarList(pillars);
        return pillarExecutor;
    }

    @Bean
    public LightCache<Pillar<Map<String, String>, ReturnData>, PayEntity> lightCache() {
        LightCache<Pillar<Map<String, String>, ReturnData>, PayEntity> lightCache = new LightCache<>();
        // 设置一级缓存个数，默认为64
//        lightCache.setL1MaxCount(64);
        // 设置一级缓存超出后丢弃的百分比，默认为0.1
//        lightCache.setL1DiscardPercent(0.1F);
        // 设置丢弃策略，默认为HotDiscard（丢弃低频热点），可选TimelineDiscard（丢弃旧时间线数据）
        lightCache.setDiscardStrategy(new TimelineDiscard<>());
        // 设置只存储到一级缓存，默认为false
//        lightCache.setOnlyCacheL1(false);
        return lightCache;
    }
}
