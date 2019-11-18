package com.github.yizzuide.milkomeda.ice;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * Job
 *
 * @author yizzuide
 * @since 1.15.0
 * Create at 2019/11/16 12:00
 */
@Data
@NoArgsConstructor
public class Job<T> implements Serializable {
    private static final long serialVersionUID = -3823440541412673211L;
    /**
     * 全局唯一ID（可以通过topic + id)
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private String id;
    /**
     * 消费者订阅的Job类型
     */
    private String topic;
    /**
     * 需要延迟的时间，单位ms
     */
    private long delay;
    /**
     * 执行超时时间，单位ms（超时后重新投入了待处理（READY）队列）
     */
    private long ttr;
    /**
     * 重试次数
     */
    private int retryCount;
    /**
     * 业务数据
     */
    private T body;
    /**
     * 状态
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private JobStatus status;

    public Job(String id, String topic, long delay, long ttr, int retryCount, T body) {
        this.id = id;
        this.topic = topic;
        this.delay = delay;
        this.ttr = ttr;
        this.retryCount = retryCount;
        this.body = body;
    }
}
