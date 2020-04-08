package com.github.yizzuide.milkomeda.particle;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;
import java.util.Map;

/**
 * ParticleProperties
 *
 * @author yizzuide
 * @since 3.0.0
 * Create at 2020/04/08 11:12
 */
@Data
@ConfigurationProperties("milkomeda.particle")
public class ParticleProperties {
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
         * 限制的路径（暂不支持，在开发计划中）
         */
        private List<String> urls;

        /**
         * 限制成功后抛出的异常（暂不支持，在开发计划中）
         */
        private Class<? extends RuntimeException> exceptionClazz;

        /**
         * 异常属性（暂不支持，在开发计划中）
         */
        private Map<String, Object> exceptionProps;

        /**
         * 限制器实例（内部使用）
         */
        LimitHandler limitHandler;

        void setLimitHandler(LimitHandler limitHandler) {
            this.limitHandler = limitHandler;
        }

        LimitHandler getLimitHandler() {
            return  this.limitHandler;
        }

    }
}
