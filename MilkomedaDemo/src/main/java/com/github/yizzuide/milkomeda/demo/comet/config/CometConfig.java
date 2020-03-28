package com.github.yizzuide.milkomeda.demo.comet.config;

import com.github.yizzuide.milkomeda.comet.CometAspect;
import com.github.yizzuide.milkomeda.comet.CometData;
import com.github.yizzuide.milkomeda.comet.CometUrlLogResolver;
import com.github.yizzuide.milkomeda.demo.comet.collector.CollectorType;
import com.github.yizzuide.milkomeda.mix.collector.Collector;
import com.github.yizzuide.milkomeda.mix.collector.CollectorFactory;
import com.github.yizzuide.milkomeda.mix.collector.CollectorRecorder;
import com.github.yizzuide.milkomeda.pillar.PillarExecutor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
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

    // UrlLog解析uid占位符
    @Bean
    public CometUrlLogResolver cometUrlLogResolver() {
        return (k, request) -> {
            if ("uid".equals(k)) {
                String token = request.getHeader("token");
                // 假设以前四位为用户id
                return StringUtils.isBlank(token) ? null : token.substring(0, 4);
            }
            return null;
        };
    }

}
