package com.github.yizzuide.milkomeda.hydrogen;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * HydrogenConfig
 *
 * @author yizzuide
 * @since 2.8.0
 * Create at 2020/03/25 17:46
 */
@Configuration
@Import({TransactionAdviceConfig.class})
@EnableConfigurationProperties(HydrogenProperties.class)
public class HydrogenConfig {
}
