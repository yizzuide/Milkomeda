package com.github.yizzuide.milkomeda.universe.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * MilkomedaAutoConfiguration
 *
 * @author yizzuide
 * @since 0.2.1
 * @version 2.0.0
 * Create at 2019/04/12 11:29
 */
@Slf4j
@Configuration
@Import(MilkomedaContextConfig.class)
@EnableConfigurationProperties(MilkomedaProperties.class)
public class MilkomedaAutoConfiguration {
}
