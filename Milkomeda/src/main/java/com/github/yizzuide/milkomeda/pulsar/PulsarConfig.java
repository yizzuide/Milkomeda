package com.github.yizzuide.milkomeda.pulsar;

import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.autoconfigure.task.TaskExecutionAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.web.context.request.async.DeferredResult;
import org.springframework.web.servlet.DispatcherServlet;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executor;

/**
 * PulsarConfig
 *
 * @author yizzuide
 * @since 1.14.0
 * @version 1.16.0
 * Create at 2019/11/11 11:34
 */
@Configuration
@ConditionalOnWebApplication
@ConditionalOnClass({DispatcherServlet.class, DeferredResult.class})
@AutoConfigureAfter(TaskExecutionAutoConfiguration.class)
public class PulsarConfig {

    @Bean
    @ConditionalOnMissingBean({Executor.class})
    public ThreadPoolTaskExecutor taskExecutor() {
        return new ThreadPoolTaskExecutor();
    }

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
