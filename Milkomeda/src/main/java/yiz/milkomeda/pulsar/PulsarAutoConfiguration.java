package yiz.milkomeda.pulsar;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.ResponseEntity;

import java.util.HashMap;
import java.util.Map;

/**
 * PulsarAutoConfiguration
 * 自动配置类
 *
 * @author yizzuide
 * Create at 2019/03/30 12:36
 */
@Configuration
public class PulsarAutoConfiguration {
    @Bean
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
