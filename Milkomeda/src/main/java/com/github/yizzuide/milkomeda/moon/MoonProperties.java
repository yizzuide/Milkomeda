package com.github.yizzuide.milkomeda.moon;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

/**
 * MoonProperties
 *
 * @author yizzuide
 * @since 3.0.0
 * Create at 2020/03/28 17:24
 */
@Data
@ConfigurationProperties("milkomeda.moon")
public class MoonProperties {
    /**
     * 实例配置
     */
    private List<Instance> instances;

    @Data
    public static class Instance {
        /**
         * 实例名（不能与现有bean有重名)
         */
        private String name;
        /**
         * Light L2缓存实例名
         */
        private String cacheName = Moon.CACHE_NAME;
        /**
         * 环形策略（默认为PeriodicMoonStrategy）
         */
        private Class<MoonStrategy> moonStrategyClazz;
        /**
         * PercentMoonStrategy百分比策略下，设置百分占值
         */
        private int percent = 100;
        /**
         * 环形阶段值
         */
        private List<Object> phases;
    }
}
