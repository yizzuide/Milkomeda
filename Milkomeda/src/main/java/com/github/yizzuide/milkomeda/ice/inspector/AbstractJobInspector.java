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

import com.github.yizzuide.milkomeda.ice.DelayJob;
import com.github.yizzuide.milkomeda.ice.JobStatus;
import org.springframework.scheduling.annotation.Async;

import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * Abstract job inspector with common code.
 *
 * @author yizzuide
 * @since 3.14.0
 * @version 3.14.1
 * <br>
 * Create at 2022/09/26 00:12
 */
public abstract class AbstractJobInspector implements JobInspector {

    private final JobStat jobStat = new JobStat();

    @Override
    public void add(JobWrapper jobWrapper, boolean update) {
        if (update) {
            jobWrapper.setUpdateTime(System.currentTimeMillis());
        }
        doAdd(jobWrapper);
    }

    @Async
    @Override
    public void initJobInspection(JobWrapper jobWrapper, Integer index) {
        jobWrapper.setBucketIndex(index);
        this.add(jobWrapper, false);
        // record stat
        jobStat.addJob(jobWrapper);
    }

    @Async
    @Override
    public void updateJobInspection(List<DelayJob> delayJobs, Integer index) {
        updateJobInspection(delayJobs, index, null, null);
    }

    @Async
    @Override
    public void updateJobInspection(List<DelayJob> delayJobs, Integer index, JobStatus status, Consumer<JobWrapper> customizer) {
        List<JobWrapper> jobWrappers = delayJobs.stream().map(delayJob -> {
            JobWrapper jobWrapper = this.get(delayJob.getJodId());
            if (index != null) {
                jobWrapper.setBucketIndex(index);
            }
            jobWrapper.setUpdateTime(System.currentTimeMillis());
            jobWrapper.setExecutionTime(delayJob.getDelayTime());
            jobWrapper.setHadRetryCount(delayJob.getRetryCount());
            if (status == null) {
                jobWrapper.setQueueType(JobQueueType.DelayQueue);
                jobWrapper.setNeedRePush(false);
            } else {
                jobWrapper.changeQueueType(status);
            }
            if (customizer != null) {
                customizer.accept(jobWrapper);
            }
            return jobWrapper;
        }).collect(Collectors.toList());

        // do update list action
        doUpdate(jobWrappers);

        // record stat
        jobWrappers.forEach(jobStat::update);
    }

    @Override
    public void finish(List<String> jobIds) {
        finishRecord(jobIds, null);
    }

    protected void finishAndRetain(List<String> jobIds) {
        finishRecord(jobIds, jobWrapper -> {
            jobWrapper.setUpdateTime(System.currentTimeMillis());
            jobWrapper.changeQueueType(JobStatus.DELETED);
            // do update action
            doUpdate(Collections.singletonList(jobWrapper));
        });
    }

    protected void finishRecord(List<String> jobIds, Consumer<JobWrapper> handler) {
        jobIds.forEach(jobId -> {
            JobWrapper jobWrapper = this.get(jobId);
            if (handler != null) {
                handler.accept(jobWrapper);
            }
            // record stat
            jobStat.finishJob(jobWrapper);
        });
    }

    /**
     * Do add a record row.
     * @param jobWrapper job wrapper info.
     */
    protected abstract void doAdd(JobWrapper jobWrapper);

    /**
     * Do Update list.
     * @param jobWrappers job wrapper list.
     */
    protected abstract void doUpdate(List<JobWrapper> jobWrappers);
}
