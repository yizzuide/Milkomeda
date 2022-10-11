package com.github.yizzuide.milkomeda.demo.particle.config;

/**
 * ParticleConfig
 * Java代码配置（改用YML配置）
 *
 * @author yizzuide
 * <br>
 * Create at 2019/05/30 15:44
 */
//@Configuration
public class ParticleConfig {

    // 去重限制器`IdempotentLimiter`框架内部配置了一份，不作为限制器链时可省略

    // 配置次数限制器
    // 一分钟限制调用3次，这个根据自己业务配置，TimesType支持的时间单位有：秒、分、时、天
    /*@Bean
    public TimesLimiter timesLimiter() {
        return new TimesLimiter(TimesType.MIN, 3L);
    }*/

    // 下面配置的限制器链为：linkIdempotentLimiter -> timesLimiter
    /*@Bean
    public BarrierLimiter barrierLimiter(TimesLimiter timesLimiter) {
        // 这里的`IdempotentLimiter`需要再创建一份用于限制器链组装，防止污染框架自动注册的Limiter的next值
        IdempotentLimiter linkIdempotentLimiter = new IdempotentLimiter();
        BarrierLimiter barrierLimiter = new BarrierLimiter();
        barrierLimiter.addLimitHandlerList(Arrays.asList(linkIdempotentLimiter, timesLimiter));
        return barrierLimiter;
    }*/
}
