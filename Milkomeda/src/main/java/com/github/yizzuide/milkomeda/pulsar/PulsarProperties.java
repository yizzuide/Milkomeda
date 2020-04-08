package com.github.yizzuide.milkomeda.pulsar;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.HashMap;
import java.util.Map;

/**
 * PulsarProperties
 *
 * @author yizzuide
 * @since 3.0.0
 * Create at 2020/04/08 22:49
 */
@Data
@ConfigurationProperties("milkomeda.pulsar")
public class PulsarProperties {
    /**
     * 超时处理
     */
    private Timeout timeout = new Timeout();

    @Data
    public static class Timeout {
        /**
         * 超时响应信息
         */
        private Map<String, Object> response = new HashMap<>();
    }
}
