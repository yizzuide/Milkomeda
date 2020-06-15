package com.github.yizzuide.milkomeda.demo.metal;

import com.github.yizzuide.milkomeda.metal.MetalHolder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * MetalContextListener
 *
 * @author yizzuide
 * Create at 2020/05/21 23:38
 */
@Slf4j
@Component
public class MetalContextListener implements ApplicationListener<ContextRefreshedEvent> {
    @Override
    public void onApplicationEvent(@NonNull ContextRefreshedEvent event) {
        if (event.getApplicationContext().getParent() == null) {
            return;
        }
        log.info("正加载配置源...");
        // 模拟配置源
        Map<String, String> source = new HashMap<>();
        source.put("env", "dev");
        source.put("version", "1.0");
        source.put("platform", "mk");
        source.put("fee_rate_config", "{\"service\":0.53, \"inst\": 0.3}");
        MetalHolder.init(source);
    }
}
