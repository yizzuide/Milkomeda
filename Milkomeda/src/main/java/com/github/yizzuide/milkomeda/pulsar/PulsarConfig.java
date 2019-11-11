package com.github.yizzuide.milkomeda.pulsar;

import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.ResponseEntity;
import org.springframework.web.context.request.async.DeferredResult;
import org.springframework.web.servlet.DispatcherServlet;

import java.util.HashMap;
import java.util.Map;

/**
 * PulsarConfig
 *
 * @author yizzuide
 * @since 1.14.0
 * Create at 2019/11/11 11:34
 */
@Configuration
@ConditionalOnWebApplication
@ConditionalOnClass({DispatcherServlet.class, DeferredResult.class})
public class PulsarConfig {
    @Bean
    public Pulsar pulsar() {
        Pulsar pulsar = new Pulsar();
        pulsar.setTimeoutCallback(() -> {
            Map<String, Object> ret = new HashMap<>();
            ret.put("error_message", "PulsarFlow handle timeout");
            return ResponseEntity.status(500).body(ret);
        });
        pulsar.setErrorCallback((Throwable t) -> {
            Map<String, Object> ret = new HashMap<>();
            ret.put("error_message", t.getMessage());
            return ResponseEntity.status(500).body(ret);
        });
        return pulsar;
    }
}
