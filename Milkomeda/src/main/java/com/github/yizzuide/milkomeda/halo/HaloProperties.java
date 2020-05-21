package com.github.yizzuide.milkomeda.halo;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.convert.DurationUnit;

import java.time.Duration;
import java.time.temporal.ChronoUnit;

/**
 * HaloProperties
 *
 * @author yizzuide
 * @since 3.5.2
 * Create at 2020/05/21 14:04
 */
@Data
@ConfigurationProperties("milkomeda.halo")
public class HaloProperties {
    /**
     * 显示慢sql日志
     */
    private boolean showSlowLog = true;

    /**
     * 慢sql阀值
     */
    @DurationUnit(ChronoUnit.MILLIS)
    private Duration slowThreshold = Duration.ofMillis(1000);
}
