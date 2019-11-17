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
     * 延迟中（等待时钟周期）
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
