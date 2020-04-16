package com.github.yizzuide.milkomeda.ice;

/**
 * JobStatus
 *
 * @author yizzuide
 * @since 1.15.0
 * Create at 2019/11/16 12:53
 */
public enum JobStatus {
    /**
     * 延迟中（准备状态、TTR Overload休眠状态）
     */
    DELAY,
    /**
     * 准备消费（可执行状态）
     */
    READY,
    /**
     * 消费中（已被消费者读取）
     */
    RESERVED,
    /**
     * 消费完成（由于删除是即时的，所以状态用不上）
     */
    DELETED
}
