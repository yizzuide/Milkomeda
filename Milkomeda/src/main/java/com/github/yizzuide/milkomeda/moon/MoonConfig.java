package com.github.yizzuide.milkomeda.moon;

import com.github.yizzuide.milkomeda.universe.context.ApplicationContextHolder;
import com.github.yizzuide.milkomeda.universe.context.WebContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.CollectionUtils;

import java.util.List;

/**
 * MoonConfig
 *
 * @author yizzuide
 * @since 3.0.0
 * Create at 2020/03/28 17:40
 */
@Slf4j
@Configuration
@EnableConfigurationProperties(MoonProperties.class)
public class MoonConfig {

    @SuppressWarnings("all")
    @Autowired
    public void config(MoonProperties moonProperties) {
        List<MoonProperties.Instance> instances = moonProperties.getInstances();
        if (CollectionUtils.isEmpty(instances)) {
            return;
        }
        for (MoonProperties.Instance instance : instances) {
            if (CollectionUtils.isEmpty(instance.getPhases())) continue;
            String beanName = instance.getName();
            String cacheName = instance.getCacheName();
            Class<MoonStrategy> moonStrategyClazz = instance.getMoonStrategyClazz();
            Moon moon = WebContext.registerBean((ConfigurableApplicationContext) ApplicationContextHolder.get(), beanName, Moon.class);
            moon.setCacheName(cacheName);
            if (moonStrategyClazz != null) {
                try {
                    MoonStrategy moonStrategy = moonStrategyClazz.newInstance();
                    if (moonStrategy instanceof PercentMoonStrategy) {
                        ((PercentMoonStrategy) moonStrategy).setPercent(instance.getPercent());
                    }
                    moon.setMoonStrategy(moonStrategy);
                } catch (Exception e) {
                    log.error("Moon invoke error with msg: {}", e.getMessage(), e);
                }
            }
            if (CollectionUtils.isEmpty(instance.getPhases())) {
                return;
            }
            moon.add(instance.getPhases().toArray(new Object[0]));
        }
    }
}
