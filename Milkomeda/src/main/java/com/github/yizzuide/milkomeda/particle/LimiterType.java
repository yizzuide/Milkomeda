package com.github.yizzuide.milkomeda.particle;

/**
 * LimiterType
 * 限制器类型
 *
 * @author yizzuide
 * @since 3.1.2
 * Create at 2020/04/22 14:14
 */
public enum LimiterType {
    /**
     * 幂等去重限制器
     */
    IDEMPOTENT,

    /**
     * 次数限制器
     */
    TIMES,

    /**
     * 组合限制器
     */
    BARRIER
}
