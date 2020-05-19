package com.github.yizzuide.milkomeda.jupiter;

import org.springframework.context.annotation.Bean;

/**
 * JupiterConfig
 *
 * @author yizzuide
 * Create at 2020/05/19 16:57
 */
public class JupiterConfig {
    @Bean
    public JupiterRuleEngine jupiterRuleEngine() {
        return new JupiterRuleELEngine();
    }
}
