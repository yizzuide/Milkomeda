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
import com.github.yizzuide.milkomeda.universe.metadata.BeanIds;

import java.util.List;
import java.util.function.Consumer;

/**
 *  Inspect accessor for job.
 *
 * @author yizzuide
 * @since 3.14.0
 * <br>
 * Create at 2022/09/14 16:55
 */
public interface JobInspector {

    /**
     * Selectable index type.
     */
    enum IndexType {
        /**
         * Time of job status updated.
         */
        UPDATE_TIME,
        /**
         * Time of job first add to job pool.
         */
        PUSH_TIME
    }

    /**
     *  Job inspection store type.
     */
    enum InspectorType {
        /**
         * Custom job inspection store, you need to provide a bean instance with `jobInspector` bean id from {@linkplain BeanIds}
         */
        AUTO_CHECK,
        /**
         * Redis impl.
         */
        REDIS,
        /**
         * Mysql impl.
         */
        MYSQL,
        /**
         * MongoDB impl.
         */
        MONGODB
    }

    /**
     * Add job wrapper while add job.
     * @param jobWrapper    JobWrapper
     * @param update        Is for update stage
     */
    void add(JobWrapper jobWrapper, boolean update);

    /**
     * Get jobs total size.
     * @return jobs size.
     */
    long size();

    /**
     * Get job wrapper from cache.
     * @param jobId job id
     * @return  JobWrapper
     */
    JobWrapper get(String jobId);

    /**
     * Get job wrapper list from cache.
     * @param start page start index, begin at 1
     * @param size size of pre page
     * @param order sorting of corresponding column, 1 is asc and -1 is desc
     * @return JobWrapper list
     */
    List<JobWrapper> getPage(int start, int size, int order);

    /**
     * Remove job wrapper while job task is completed.
     * @param jobIds    job id array
     */
    void finish(List<String> jobIds);

    /**
     * Init job inspection info.
     * @param jobWrapper    job wrapper
     * @param index         bucket index
     */
    void initJobInspection(JobWrapper jobWrapper, Integer index);

    /**
     * Update job inspection info.
     * @param delayJobs delay job list
     * @param index bucket index
     */
    void updateJobInspection(List<DelayJob> delayJobs, Integer index);

    /**
     * Update job inspection info.
     * @param delayJobs delay job list
     * @param index bucket index
     * @param status job status
     * @param customizer custom setting function
     */
    void updateJobInspection(List<DelayJob> delayJobs, Integer index, JobStatus status, Consumer<JobWrapper> customizer);
}
