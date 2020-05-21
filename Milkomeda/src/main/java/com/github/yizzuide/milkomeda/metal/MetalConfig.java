package com.github.yizzuide.milkomeda.metal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * MetalConfig
 *
 * @author yizzuide
 * @since 3.6.0
 * Create at 2020/05/21 23:26
 */
@Configuration
public class MetalConfig {

    @Bean
    public MetalSource metalSource() {
        return new MetalSource();
    }

    @Bean
    public MetalContainer metalContainer(MetalSource metalSource) {
        return new MetalContainer(metalSource);
    }

    @Bean
    public MetalRegister metalRegister(MetalContainer metalContainer) {
        return new MetalRegister(metalContainer);
    }

    @Autowired
    public void config(MetalContainer metalContainer) {
        MetalHolder.setMetalContainer(metalContainer);
    }
}
