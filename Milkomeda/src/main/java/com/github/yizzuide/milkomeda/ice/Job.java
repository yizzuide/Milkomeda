/*
 * Copyright (c) 2021 yizzuide All rights Reserved.
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.github.yizzuide.milkomeda.ice;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * Job
 * 一个被创建的包装业务数据的延迟任务类
 *
 * @author yizzuide
 * @since 1.15.0
 * @version 3.14.0
 * <br>
 * Create at 2019/11/16 12:00
 */
@Data
@NoArgsConstructor
public class Job<T> implements Serializable, Cloneable {
    private static final long serialVersionUID = -3823440541412673211L;
    /**
     * 全局唯一ID（内部会将topic拼接进来以保证唯一性：topic-id，长度最好小于62，因为存储重试次数多占了两位，用于Redis的 ziplist 内存存储优化）
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private String id;
    /**
     * 消费者订阅的Job类型（命名类型建议：产品名_业务名_topic）
     */
    private String topic;
    /**
     * 延迟时间，单位ms
     */
    private long delay;
    /**
     * 执行超时时间，单位ms（超时后重新投入了待处理（READY）队列）
     */
    private long ttr;
    /**
     * 初始重试次数
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

    /**
     * Restricted external access.
     * @param status    job status
     */
    void setStatus(JobStatus status) {
        this.status = status;
    }

    public Job(String id, String topic, long delay, long ttr, int retryCount, T body) {
        this.id = id;
        this.topic = topic;
        this.delay = delay;
        this.ttr = ttr;
        this.retryCount = retryCount;
        this.body = body;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Job<T> clone() {
        try {
            Job<T> clone = (Job<T>) super.clone();
            // merge jobId -> jobId
            clone.setId(Ice.getId(clone.getId()));
            return clone;
        } catch (CloneNotSupportedException e) {
            throw new AssertionError("Job clone error with msg: " + e.getMessage());
        }
    }
}
