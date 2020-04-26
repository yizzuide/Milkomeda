package com.github.yizzuide.milkomeda.ice;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * DelayJob
 *
 * @author yizzuide
 * @since 1.15.0
 * Create at 2019/11/16 15:30
 */
@Data
@NoArgsConstructor
class DelayJob implements Serializable {
    private static final long serialVersionUID = -1408197881231593037L;

    /**
     * 延迟任务的唯一标识
     */
    private String jodId;

    /**
     * 任务的执行时间（单位：ms）
     */
    private long delayTime;

    /**
     * 任务分组
     */
    private String topic;

    /**
     * 当前重试次数
     */
    private int retryCount;

    @SuppressWarnings("rawtypes")
    DelayJob(Job job) {
        this.jodId = job.getId();
        this.delayTime = System.currentTimeMillis() + job.getDelay();
        this.topic = job.getTopic();
    }
}
