package com.github.yizzuide.milkomeda.atom;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * ZkAtomConfig
 *
 * @author yizzuide
 * Create at 2020/05/01 12:27
 */
@Configuration
@EnableConfigurationProperties(AtomProperties.class)
@ConditionalOnProperty(prefix = "milkomeda.atom", name = "strategy", havingValue = "ZK")
public class ZkAtomConfig {

}
