package com.github.yizzuide.milkomeda.ice;

import com.github.yizzuide.milkomeda.universe.config.MilkomedaContextConfig;
import com.github.yizzuide.milkomeda.universe.context.ApplicationContextHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.core.RedisTemplate;

/**
 * IceConfig
 *
 * @author yizzuide
 * @since 1.15.0
 * @version 2.0.0
 * Create at 2019/11/16 17:19
 */
@Configuration
@Import(MilkomedaContextConfig.class)
@ConditionalOnClass(RedisTemplate.class)
@AutoConfigureAfter(RedisAutoConfiguration.class)
@EnableConfigurationProperties(IceProperties.class)
public class IceConfig extends IceBasicConfig {

    @Autowired
    private ApplicationContextHolder applicationContextHolder;

    @Bean
    @ConditionalOnProperty(prefix = "milkomeda.ice", name = "enable-task", havingValue = "true")
    public IceContext iceContext() {
        return new IceContext();
    }

    @Bean
    @ConditionalOnProperty(prefix = "milkomeda.ice", name = "enable-job-timer", havingValue = "true", matchIfMissing = true)
    public DelayTimer delayTimer() {
        return new DelayTimer();
    }
}
