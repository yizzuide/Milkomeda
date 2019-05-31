package com.github.yizzuide.milkomeda.particle;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Particle
 * 粒子状态数据
 *
 * @author yizzuide
 * @since 1.5.0
 * Create at 2019/05/30 13:44
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Particle {
    /**
     * 状态类型
     */
    private Class<? extends Limiter> type;

    /**
     * 是否被限制
     */
    private boolean limited;

    /**
     * 结果值
     */
    private Object value;
}
