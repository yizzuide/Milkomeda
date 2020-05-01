package com.github.yizzuide.milkomeda.atom;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * AtomConfig
 *
 * @author yizzuide
 * @since 3.3.0
 * Create at 2020/04/30 15:13
 */
@Configuration
@Import({RedisAtomConfig.class, ZkAtomConfig.class})
public class AtomConfig {

    @Bean
    public AtomLockAspect atomLockAspect() {
        return new AtomLockAspect();
    }

}
