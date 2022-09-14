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

package com.github.yizzuide.milkomeda.ice;

import com.github.yizzuide.milkomeda.universe.context.ApplicationContextHolder;
import com.github.yizzuide.milkomeda.util.JSONUtil;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationListener;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.util.CollectionUtils;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * Redis impl of job introspection information cache.
 *
 * @author yizzuide
 * @since 3.14.0
 * Create at 2022/09/14 16:58
 */
public class RedisJobInspector implements JobInspector, InitializingBean, ApplicationListener<IceInstanceChangeEvent> {

    private StringRedisTemplate redisTemplate;

    public static final String JOB_INSPECTOR_CURSOR_KEY_PREFIX = "ice:job_inspect:cursor:";

    public static final String JOB_INSPECTOR_Data_KEY_PREFIX = "ice:job_inspect:data:";

    private String jobInspectorCursorKey;

    private String jobInspectorDataKey;

    public RedisJobInspector(IceProperties props) {
        if (!IceProperties.DEFAULT_INSTANCE_NAME.equals(props.getInstanceName())) {
            this.jobInspectorCursorKey = JOB_INSPECTOR_CURSOR_KEY_PREFIX + props.getInstanceName();
            this.jobInspectorDataKey = JOB_INSPECTOR_Data_KEY_PREFIX + props.getInstanceName();
        }
    }


    @Override
    public void add(JobWrapper jobWrapper, boolean update) {
        if (update) {
            jobWrapper.setUpdateTime(System.currentTimeMillis());
        }
        redisTemplate.boundZSetOps(jobInspectorCursorKey).add(jobWrapper.getId(), jobWrapper.getUpdateTime());
        redisTemplate.boundHashOps(jobInspectorDataKey).put(jobWrapper.getId(), JSONUtil.serialize(jobWrapper));
    }

    public JobWrapper get(String jobId) {
        BoundHashOperations<String, String, String> ops = redisTemplate.boundHashOps(jobInspectorDataKey);
        return JSONUtil.parse(ops.get(jobId), JobWrapper.class);
    }

    public List<JobWrapper> getPage(int start, int size) {
        int pageCount = start * size;
        Set<String> jobIds = redisTemplate.boundZSetOps(jobInspectorCursorKey).reverseRange(pageCount, pageCount + size);
        if (CollectionUtils.isEmpty(jobIds)) {
            return Collections.emptyList();
        }
        return jobIds.stream().map(this::get).collect(Collectors.toList());
    }

    @Async
    @Override
    public void finish(List<String> jobIds) {
        redisTemplate.boundZSetOps(jobInspectorCursorKey).remove(jobIds.toArray());
        redisTemplate.boundHashOps(jobInspectorDataKey).delete(jobIds.toArray());
    }

    @Async
    public void initJobInspection(JobWrapper jobWrapper, Integer index) {
        jobWrapper.setBucketIndex(index);
        this.add(jobWrapper, false);
    }

    @Async
    public void updateJobInspection(List<DelayJob> delayJobs, Integer index) {
        updateJobInspection(delayJobs, index, null, null);
    }

    @Async
    public void updateJobInspection(List<DelayJob> delayJobs, Integer index, JobStatus status, Consumer<JobWrapper> customizer) {
        delayJobs.forEach(delayJob -> {
            JobWrapper jobWrapper = this.get(delayJob.getJodId());
            if (index != null) {
                jobWrapper.setBucketIndex(index);
            }
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
            this.add(jobWrapper, true);
        });
    }

    @Override
    public void afterPropertiesSet() {
        redisTemplate = ApplicationContextHolder.get().getBean(StringRedisTemplate.class);
    }

    @Override
    public void onApplicationEvent(IceInstanceChangeEvent event) {
        String instanceName = event.getSource().toString();
        jobInspectorCursorKey = JOB_INSPECTOR_CURSOR_KEY_PREFIX + instanceName;
        this.jobInspectorDataKey = JOB_INSPECTOR_Data_KEY_PREFIX + instanceName;
    }
}
