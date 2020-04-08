package com.github.yizzuide.milkomeda.pulsar;

import com.github.yizzuide.milkomeda.universe.yml.YmlParser;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.autoconfigure.task.TaskExecutionAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
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
 * @version 3.0.0
 * Create at 2019/11/11 11:34
 */
@Configuration
@ConditionalOnWebApplication
@ConditionalOnClass({DispatcherServlet.class, DeferredResult.class})
@AutoConfigureAfter(TaskExecutionAutoConfiguration.class)
@EnableConfigurationProperties(PulsarProperties.class)
public class PulsarConfig {

    @Bean
    @ConditionalOnMissingBean({Executor.class})
    public ThreadPoolTaskExecutor taskExecutor() {
        return new ThreadPoolTaskExecutor();
    }

    @Bean
    public Pulsar pulsar(PulsarProperties pulsarProperties) {
        Pulsar pulsar = new Pulsar();
        pulsar.setTimeoutCallback(() -> {
            Map<String, Object> response = pulsarProperties.getTimeout().getResponse();
            Object status = response.get("status");
            status = status == null ?  503 : status;
            Map<String, Object> result = new HashMap<>();
            YmlParser.parseAliasMapPath(response, result, "code", -1, null);
            YmlParser.parseAliasMapPath(response, result, "message", "服务请求响应超时，请稍后再试！", null);
            return ResponseEntity.status(Integer.parseInt(status.toString())).body(result);
        });
        return pulsar;
    }
}
