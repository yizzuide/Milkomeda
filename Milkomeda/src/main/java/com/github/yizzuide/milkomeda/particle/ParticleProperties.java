package com.github.yizzuide.milkomeda.particle;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.convert.DurationUnit;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;

/**
 * ParticleProperties
 *
 * @author yizzuide
 * @since 3.0.0
 * @version 3.0.3
 * Create at 2020/04/08 11:12
 */
@Data
@ConfigurationProperties("milkomeda.particle")
public class ParticleProperties {
    /**
     * 开启请求过滤
     */
    private boolean enableFilter = false;

    /**
     * 排除的URL
     */
    private List<String> excludeUrls;

    /**
     * 包含的URL
     */
    private List<String> includeUrls;

    /**
     * 限制后的响应
     */
    private Map<String, Object> response;

    /**
     * 限制处理器列表
     */
    private List<Limiter> limiters;


    @Data
    public static class Limiter {
        /**
         * 限制处理器Bean名（用于注解方式或lazy注入，Bean名不可重复）
         */
        private String name;
        /**
         * 限制处理器类
         */
        private Class<? extends LimitHandler> handlerClazz;

        /**
         * 限制处理器属性
         */
        private Map<String, Object> props;

        /**
         * 分布式key模板
         */
        private String keyTpl = "limit_{method}_{uri}_(token)";

        /**
         * 分布式key过期时间
         */
        @DurationUnit(ChronoUnit.SECONDS)
        private Duration keyExpire = Duration.ofSeconds(60);

        /**
         * 限制的路径
         */
        private List<String> urls;

        /**
         * 限制器实例（内部使用）
         */
        private LimitHandler limitHandler;

        /**
         * 缓存占位符（内部使用）
         */
        private Map<String, List<String>> cacheKeys;

        void setLimitHandler(LimitHandler limitHandler) {
            this.limitHandler = limitHandler;
        }

        LimitHandler getLimitHandler() {
            return  this.limitHandler;
        }

        void setCacheKeys(Map<String, List<String>> cacheKeys) {
            this.cacheKeys = cacheKeys;
        }

        Map<String, List<String>> getCacheKeys() {
            return this.cacheKeys;
        }

    }
}
