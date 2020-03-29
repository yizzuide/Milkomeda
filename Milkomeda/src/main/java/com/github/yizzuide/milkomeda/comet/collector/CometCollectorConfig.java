package com.github.yizzuide.milkomeda.comet.collector;

import com.github.yizzuide.milkomeda.comet.core.CometAspect;
import com.github.yizzuide.milkomeda.comet.core.CometConfig;
import com.github.yizzuide.milkomeda.comet.core.CometData;
import com.github.yizzuide.milkomeda.pillar.PillarExecutor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

/**
 * CometCollectorConfig
 *
 * @author yizzuide
 * @since 2.8.0
 * Create at 2020/03/28 18:54
 */
@Configuration
@AutoConfigureAfter(CometConfig.class)
@EnableConfigurationProperties(CometCollectorProperties.class)
public class CometCollectorConfig {

    @Autowired(required = false)
    private List<Collector> collectors;

    @Bean
    public CollectorFactory collectorFactory() {
        PillarExecutor<CometData, Object> pillarExecutor = new PillarExecutor<>();
        if (!CollectionUtils.isEmpty(collectors)) {
            pillarExecutor.addPillarList(collectors.stream().filter(c -> StringUtils.hasLength(c.supportType())).collect(Collectors.toList()));
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

    @Bean
    @ConditionalOnProperty(prefix = "milkomeda.comet.collector", name = "enable-tag", havingValue = "true")
    public CometCollectorResponseBodyAdvice cometResponseBodyAdvice() {
        return new CometCollectorResponseBodyAdvice();
    }
}
