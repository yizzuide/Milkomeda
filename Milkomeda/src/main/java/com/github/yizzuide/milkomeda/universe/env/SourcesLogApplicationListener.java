package com.github.yizzuide.milkomeda.universe.env;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.config.ConfigFileApplicationListener;
import org.springframework.boot.context.event.ApplicationEnvironmentPreparedEvent;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.context.ApplicationListener;
import org.springframework.core.Ordered;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.StandardEnvironment;

/**
 * ParticleApplicationListener
 *
 * @author yizzuide
 * @since 3.0.1
 * @see org.springframework.boot.context.config.ConfigFileApplicationListener
 * @see org.springframework.boot.context.config.AnsiOutputApplicationListener
 * Create at 2020/04/11 11:56
 */
@Slf4j
public class SourcesLogApplicationListener implements ApplicationListener<ApplicationEnvironmentPreparedEvent>, Ordered {

    @Override
    public void onApplicationEvent(ApplicationEnvironmentPreparedEvent event) {
        ConfigurableEnvironment environment = event.getEnvironment();
        if (environment.getClass() == StandardEnvironment.class) {
            return;
        }

        // StandardServletEnvironment or StandardReactiveEnvironment
        // 绑定类型
        boolean logEnable = Binder.get(environment).bind("milkomeda.show-log", Boolean.class).orElseGet(() -> false);
        log.info("milkomeda log is {}", logEnable ? "enable" : "disable");
        log.info("Load sources: {}", environment.getPropertySources());
    }

    @Override
    public int getOrder() {
        // Apply after ConfigFileApplicationListener has called EnvironmentPostProcessors
        return ConfigFileApplicationListener.DEFAULT_ORDER + 10;
    }
}
