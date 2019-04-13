package com.github.yizzuide.milkomeda.universe.config;

import com.github.yizzuide.milkomeda.pulsar.Pulsar;
import com.github.yizzuide.milkomeda.universe.context.ApplicationContextHolder;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.ResponseEntity;

import java.util.HashMap;
import java.util.Map;

/**
 * MilkomedaAutoConfiguration
 *
 * @author yizzuide
 * @since 0.2.1
 * Create at 2019/04/12 11:29
 */
@Configuration
public class MilkomedaAutoConfiguration {
    @Bean
    public ApplicationContextHolder applicationContextHolder() {
        return new ApplicationContextHolder();
    }

    @Bean
    @ConditionalOnClass(Pulsar.class)
    Pulsar pulsar() {
        Pulsar pulsar = new Pulsar();
        pulsar.setTimeoutCallback(() -> {
            Map<String, Object> ret = new HashMap<>();
            ret.put("errorMsg", "PulsarAsync handle timeout");
            return ResponseEntity.status(500).body(ret);
        });
        return pulsar;
    }
}
