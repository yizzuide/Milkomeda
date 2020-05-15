package com.github.yizzuide.milkomeda.particle;

import java.util.List;
import java.util.Map;

/**
 * LimiterConfigSelector
 *
 * @author yizzuide
 * @since 3.5.0
 * Create at 2020/05/14 17:47
 */
public class LimiterConfigSelector {

    /**
     * 查询组合链的配置Limiter
     * @param handlerClazz  当前限制器
     * @param chain         限制器链名
     * @param props         配置
     * @return  Limiter
     */
    public static ParticleProperties.Limiter barrierSelect(Class<? extends Limiter> handlerClazz, List<String> chain, ParticleProperties props) {
        for (Map.Entry<String, LimitHandler> entry : ParticleConfig.getCacheHandlerBeans().entrySet()) {
            if (entry.getValue().getClass() != handlerClazz) {
                continue;
            }
            if (!chain.contains(entry.getKey())) {
                continue;
            }
            return props.getLimiters().stream().filter(limiter -> limiter.getName().equals(entry.getKey())).findFirst().orElse(null);
        }
        return null;
    }
}
