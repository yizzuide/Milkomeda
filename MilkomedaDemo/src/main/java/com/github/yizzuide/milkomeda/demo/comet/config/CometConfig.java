package com.github.yizzuide.milkomeda.demo.comet.config;

import com.github.yizzuide.milkomeda.comet.CometAspect;
import com.github.yizzuide.milkomeda.comet.CometData;
import com.github.yizzuide.milkomeda.comet.CometRecorder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * CometConfig
 *
 * @author yizzuide
 * Create at 2019/04/11 22:18
 */
@Slf4j
@Configuration
public class CometConfig {
    @Bean
    public CometAspect cometAspect() {
        CometAspect cometAspect = new CometAspect();
        cometAspect.setRecorder(new CometRecorder() {
            @Override
            public void onRequest(CometData cometData) {
                log.info("onRequest {}", cometData);
                // 根据是否覆盖 prototype 方法替换采集日志实体，这里可以根据业务添加相应设置
            }

            @Override
            public void onReturn(CometData cometData) {
                log.info("onReturn {}", cometData);
            }

            @Override
            public void onThrowing(CometData cometData) {
                log.error("onThrowing {}", cometData);
            }
        });
        return cometAspect;
    }
}
