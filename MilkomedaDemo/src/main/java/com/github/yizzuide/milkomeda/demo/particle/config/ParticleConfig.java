package com.github.yizzuide.milkomeda.demo.particle.config;

import com.github.yizzuide.milkomeda.particle.BarrierLimiter;
import com.github.yizzuide.milkomeda.particle.IdempotentLimiter;
import com.github.yizzuide.milkomeda.particle.TimesLimiter;
import com.github.yizzuide.milkomeda.particle.TimesType;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;

/**
 * ParticleConfig
 *
 * @author yizzuide
 * Create at 2019/05/30 15:44
 */
@Configuration
public class ParticleConfig {

    @Bean
    public TimesLimiter timesLimiter() {
        return new TimesLimiter(TimesType.MIN, 3L);
    }

    @Bean
    public BarrierLimiter barrierLimiter(IdempotentLimiter idempotentLimiter, TimesLimiter timesLimiter) {
        BarrierLimiter barrierLimiter = new BarrierLimiter();
        barrierLimiter.addLimitHandlerList(Arrays.asList(idempotentLimiter, timesLimiter));
        return barrierLimiter;
    }
}
