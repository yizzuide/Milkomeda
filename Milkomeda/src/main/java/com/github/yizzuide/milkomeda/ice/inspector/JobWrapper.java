/*
 * Copyright (c) 2022 yizzuide All rights Reserved.
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

package com.github.yizzuide.milkomeda.ice.inspector;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.github.yizzuide.milkomeda.ice.IceHolder;
import com.github.yizzuide.milkomeda.ice.Job;
import com.github.yizzuide.milkomeda.ice.JobStatus;
import lombok.Data;

import java.io.Serializable;

/**
 * Job introspect info with {@link Job}
 *
 * @author yizzuide
 * @since 3.14.0
 * <br />
 * Create at 2022/09/14 00:47
 */
@Data
public class JobWrapper implements Serializable {

    private static final long serialVersionUID = 7740940341374290266L;

    /**
     * Job id
     */
    private String id;

    /**
     * Topic of job which fetch from client.
     */
    private String topic;

    /**
     * Name of application.
     */
    private String applicationName;

    /**
     * Current exists in queue type.
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private JobQueueType queueType;

    /**
     * Is need to push again?
     */
    private boolean needRePush;

    /**
     * Time of add to job pool.
     */
    public long pushTime;

    /**
     *  Update time for job status.
     */
    private long updateTime;

    /**
     * Next time of task execution in delay queue. -1 if queue type is not `DelayQueue`.
     */
    private long executionTime;

    /**
     * Index of delay job bucket in delay queue, -1 if queue type is not `DelayQueue`.
     */
    private int bucketIndex;

    /**
     *  Number of retries.
     */
    private int hadRetryCount;

    /**
     * build instance from {@link Job}
     * @param job   Job
     * @return  JobWrapper
     */
    public static JobWrapper buildFrom(Job<?> job) {
        JobWrapper jobWrapper = new JobWrapper();
        jobWrapper.setId(job.getId());
        jobWrapper.setTopic(job.getTopic());
        jobWrapper.setApplicationName(IceHolder.getApplicationName());
        jobWrapper.setPushTime(System.currentTimeMillis());
        jobWrapper.setUpdateTime(jobWrapper.getPushTime());
        jobWrapper.setExecutionTime(jobWrapper.getPushTime() + job.getDelay());
        jobWrapper.setNeedRePush(false);
        jobWrapper.changeQueueType(job.getStatus());
        return jobWrapper;
    }

    /**
     * Update queue type from job status
     * @param status    JobStatus
     */
    public void changeQueueType(JobStatus status) {
        switch (status) {
            case DELAY:
            case RESERVED:
                this.setQueueType(JobQueueType.DelayQueue);
                break;
            case READY: {
                this.setQueueType(JobQueueType.ReadyQueue);
                this.setNeedRePush(false);
                this.setExecutionTime(-1);
                this.setBucketIndex(-1);
                break;
            }
            case IDLE: {
                this.setQueueType(IceHolder.getProps().isEnableRetainToDeadQueueWhenTtrOverload() ?
                        JobQueueType.DeadQueue : JobQueueType.NoneQueue);
                this.setNeedRePush(true);
                this.setExecutionTime(-1);
                this.setBucketIndex(-1);
                break;
            }
            case DELETED:
                // This status is unused now!
                break;
        }
    }
}
