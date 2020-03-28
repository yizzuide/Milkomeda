package com.github.yizzuide.milkomeda.mix.collector;

import com.github.yizzuide.milkomeda.comet.CometAspect;
import com.github.yizzuide.milkomeda.comet.CometConfig;
import com.github.yizzuide.milkomeda.comet.CometData;
import com.github.yizzuide.milkomeda.pillar.PillarExecutor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.CollectionUtils;

import java.util.List;

/**
 * CometCollectorConfig
 *
 * @author yizzuide
 * @since 2.8.0
 * Create at 2020/03/28 18:54
 */
@Configuration
@AutoConfigureAfter(CometConfig.class)
public class CometCollectorConfig {

    @Autowired(required = false)
    private List<Collector> collectors;

    @Bean
    public CollectorFactory collectorFactory() {
        PillarExecutor<CometData, Object> pillarExecutor = new PillarExecutor<>();
        if (!CollectionUtils.isEmpty(collectors)) {
            pillarExecutor.addPillarList(collectors);
        }
        return new CollectorFactory(pillarExecutor);
    }

    @Bean
    public CollectorRecorder collectorRecorder() {
        return new CollectorRecorder(collectorFactory());
    }

    @SuppressWarnings("all")
    @Autowired
    public void config(CometAspect cometAspect) {
        // 设置日志采集器
        cometAspect.setRecorder(collectorRecorder());
    }
}
