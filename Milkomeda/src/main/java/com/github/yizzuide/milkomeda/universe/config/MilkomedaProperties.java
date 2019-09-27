package com.github.yizzuide.milkomeda.universe.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * EchoProperties
 *
 * @author yizzuide
 * @since 1.13.2
 * Create at 2019/09/27 18:36
 */
@Data
@ConfigurationProperties("milkomeda")
public class MilkomedaProperties {
    /**
     * 是否显示日志，默认为false
     */
    private boolean showLog;
}
