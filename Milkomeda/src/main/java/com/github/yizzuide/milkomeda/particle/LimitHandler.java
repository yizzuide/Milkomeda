package com.github.yizzuide.milkomeda.particle;

import lombok.Data;

/**
 * LimitHandler
 * 限制处理器链
 *
 * @author yizzuide
 * @since 1.5.0
 * Create at 2019/05/31 01:22
 */
@Data
public abstract class LimitHandler implements Limiter {
    /**
     * 拦截链串，用于记录链条数据
     */
    protected LimitHandler next;
}
