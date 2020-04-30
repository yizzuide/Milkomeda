package com.github.yizzuide.milkomeda.atom;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Map;

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
     * 使用集群方案
     */
    private boolean useCluster = false;

    /**
     * 策略属性设置
     */
    private Map<String, Object> props;
}
