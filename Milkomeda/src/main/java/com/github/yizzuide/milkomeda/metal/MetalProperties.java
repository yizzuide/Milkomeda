package com.github.yizzuide.milkomeda.metal;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * MetalProperties
 *
 * @author yizzuide
 * @since 3.6.1
 * Create at 2020/05/23 22:54
 */
@Data
@ConfigurationProperties("milkomeda.metal")
public class MetalProperties {
    /**
     * 分布式配置服务名（如果是后台管理应用和API应用共享配置，必需设置一样的应用名）
     */
    private String applicationName;
}
