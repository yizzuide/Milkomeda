package com.github.yizzuide.milkomeda.demo.comet.config;

import com.github.yizzuide.milkomeda.comet.CometAspect;
import com.github.yizzuide.milkomeda.comet.CometData;
import com.github.yizzuide.milkomeda.demo.comet.collector.CollectorType;
import com.github.yizzuide.milkomeda.mix.collector.Collector;
import com.github.yizzuide.milkomeda.mix.collector.CollectorFactory;
import com.github.yizzuide.milkomeda.mix.collector.CollectorRecorder;
import com.github.yizzuide.milkomeda.pillar.PillarExecutor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * CometConfig
 *
 * @author yizzuide
 * Create at 2019/04/11 22:18
 */
@Slf4j
@Configuration
public class CometConfig {

    @Autowired
    private List<Collector> collectors;

    @Bean("cometPillarExecutor") // 这里取个别名（因为在PillarConfig例子中配置过了）
    public PillarExecutor<CometData, Object> pillarExecutor() {
        PillarExecutor<CometData, Object> pillarExecutor = new PillarExecutor<>();
        pillarExecutor.addPillarList(collectors);
        return pillarExecutor;
    }

    @Bean
    CollectorFactory collectorFactory(PillarExecutor<CometData, Object> pillarExecutor) {
        return new CollectorFactory(pillarExecutor, CollectorType.values());
    }

    @Bean
    CollectorRecorder logRecorder() {
        return new CollectorRecorder();
    }

    @Autowired
    public void config(CometAspect cometAspect, CollectorRecorder collectorRecorder) {
        // 设置日志采集器
        cometAspect.setRecorder(collectorRecorder);
    }

}
