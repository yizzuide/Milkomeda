package com.github.yizzuide.milkomeda.wormhole;

/**
 * WormholeTransactionType
 *
 * @author yizzuide
 * @since 3.11.0
 * Create at 2020/07/08 16:20
 */
public enum WormholeTransactionHangType {
    /**
     * 不支持事务
     */
    NONE,
    /**
     * 事务提交之前
     */
    BEFORE_COMMIT,
    /**
     * 事务提交之后
     */
    AFTER_COMMIT,
    /**
     * 事务回滚之后
     */
    AFTER_ROLLBACK,
    /**
     * 事务成功完成之后
     */
    AFTER_COMPLETION
}
