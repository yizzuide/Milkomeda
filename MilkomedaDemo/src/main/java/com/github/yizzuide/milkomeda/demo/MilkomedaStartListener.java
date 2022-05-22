package com.github.yizzuide.milkomeda.demo;

import com.github.yizzuide.milkomeda.metal.MetalHolder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * MilkomedaStartListener
 *
 * @author yizzuide
 * Create at 2022/02/10 16:18
 */
@Slf4j
@Component
public class MilkomedaStartListener implements ApplicationListener<ApplicationStartedEvent> {

    //@Lazy
    //@Resource
	//private Moon<String> smsMoon;

    @Override
    public void onApplicationEvent(@NonNull ApplicationStartedEvent event) {
        loadConfig();
    }

    private void loadConfig() {
        log.info("正加载配置源...");
        // 模拟从加数据源加载配置
        Map<String, String> source = new HashMap<>();
        source.put("env", "dev");
        source.put("version", "1.0");
        source.put("platform", "mk");
        source.put("fee_rate_config", "{\"service\":0.53, \"inst\": 0.3}");
        MetalHolder.init(source);


        // 从配置动态加载数据
//        smsMoon.add("七牛云短信", "阿里云短信", "容联云短信");
    }
}

