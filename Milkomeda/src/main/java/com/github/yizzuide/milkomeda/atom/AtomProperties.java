package com.github.yizzuide.milkomeda.atom;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.convert.DurationUnit;

import java.time.Duration;
import java.time.temporal.ChronoUnit;

/**
 * AtomProperties
 *
 * @author yizzuide
 * @since 3.3.0
 * Create at 2020/04/30 15:26
 */
@Data
@ConfigurationProperties("milkomeda.atom")
public class AtomProperties {
    /**
     * 策略方式
     */
    private AtomStrategyType strategy = AtomStrategyType.REDIS;

    /**
     * redis配置
     */
    private Redis redis = new Redis();

    /**
     * Zk配置
     */
    private Zk zk = new Zk();

    @Data
    static class Redis {
        /**
         * 使用集群方案
         */
        private boolean useCluster = false;
    }

    @Data
    static class Zk {
        /**
         * 连接地址
         */
        private String address = "127.0.0.1:2181";

        /**
         * 根节点名称
         */
        private String rootLockNode = "mk_atom_locks";

        /**
         * 重试睡眠时间
         */
        @DurationUnit(ChronoUnit.MILLIS)
        private Duration sleepTime = Duration.ofMillis(1000);

        /**
         * 最大重试次数
         */
        private int maxRetry = 3;
    }
}
