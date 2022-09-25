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

import com.fasterxml.jackson.core.type.TypeReference;
import com.github.yizzuide.milkomeda.ice.inspector.JobInspector;
import com.github.yizzuide.milkomeda.ice.inspector.JobWrapper;
import com.github.yizzuide.milkomeda.universe.polyfill.RedisPolyfill;
import com.github.yizzuide.milkomeda.util.RedisUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.util.CollectionUtils;

import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * RedisIce
 * 基于Redis的延迟队列实现
 *
 * @author yizzuide
 * @since 1.15.0
 * @version 3.14.0
 * Create at 2019/11/16 15:20
 */
@Slf4j
public class RedisIce implements Ice, ApplicationListener<IceInstanceChangeEvent> {

    @Autowired
    private JobPool jobPool;

    @Autowired
    private DelayBucket delayBucket;

    @Autowired
    private ReadyQueue readyQueue;

    @Autowired
    private DeadQueue deadQueue;

    @Autowired(required = false)
    private JobInspector jobInspector;

    @Autowired
    private StringRedisTemplate redisTemplate;

    private final IceProperties props;

    private String lockKey = "ice:range_pop_lock";

    public RedisIce(IceProperties props) {
        this.props = props;
        if (!IceProperties.DEFAULT_INSTANCE_NAME.equals(props.getInstanceName())) {
            this.lockKey = "ice:range_pop_lock:" + props.getInstanceName();
        }
    }

    @SuppressWarnings("rawtypes")
    @Override
    public void add(Job job) {
        add(job, true, true);
    }

    @SuppressWarnings("rawtypes")
    @Override
    public void add(Job job, boolean mergeIdWithTopic, boolean replaceWhenExists) {
        if (mergeIdWithTopic) {
            job.setId(job.getTopic() + IceProperties.MERGE_ID_SEPARATOR + job.getId());
        }

        DelayJob delayJob = new DelayJob(job);
        boolean hadSetStatus = job.getStatus() != null;
        if (replaceWhenExists && (hadSetStatus || jobPool.exists(job.getId()))) {
            // Using serialized job to test
            Job<?> serializedJob = hadSetStatus ? job : jobPool.get(job.getId());
            if (serializedJob.getStatus() == JobStatus.DELAY ||
                    serializedJob.getStatus() == JobStatus.READY ||
                    serializedJob.getStatus() == JobStatus.RESERVED) {
                log.warn("Ice no need to add job which Working normally, jobId: {}", job.getId());
                return;
            }
            if (props.isEnableRetainToDeadQueueWhenTtrOverload() &&
                    serializedJob.getStatus() == JobStatus.IDLE) {
                // Restore the delay time set at idle state.
                delayJob.setDelayTime(serializedJob.getDelay());
                deadQueue.remove(delayJob);
                // Update delay time.
                delayJob.updateDelayTime();
            }
        }

        job.setStatus(JobStatus.DELAY);
        JobWrapper jobWrapper = JobWrapper.buildFrom(job);

        RedisUtil.batchOps((operations) -> {
            jobPool.remove(operations, job.getId());
            jobPool.push(operations, job);
            Integer index = delayBucket.add(operations, delayJob);

            // Record job
            if (jobInspector != null) {
                jobInspector.initJobInspection(jobWrapper, index);
            }
        }, redisTemplate);
    }

    @Override
    public <T> void add(String id, String topic, T body, Duration delay) {
        add(build(id, topic, body, delay));
    }

    @Override
    public <T> void add(String id, String topic, T body, long delay) {
        add(build(id, topic, body, delay));
    }

    @Override
    public <T> Job<T> build(String id, String topic, T body, Duration delay) {
        return build(id, topic, body, delay.toMillis());
    }

    @Override
    public <T> Job<T> build(String id, String topic, T body, long delay) {
        return new Job<>(id, topic, delay, props.getTtr().toMillis(), props.getRetryCount(), body);
    }

    public void rePushJob(String jobId) {
        add(jobPool.get(jobId));
    }

    @Override
    public List<JobWrapper> getJobInspectPage(int start, int size, int order) {
        if (jobInspector == null) {
            return Collections.emptyList();
        }
        return jobInspector.getPage(start, size, order);
    }

    @Override
    public Job<?> getJobDetail(String jobId) {
        return jobPool.get(jobId);
    }

    public Map<String, String> getCacheKey(String jobId) {
        if (jobInspector == null) {
            return Collections.emptyMap();
        }
        JobWrapper jobWrapper = jobInspector.get(jobId);
        if (jobWrapper == null) {
            return Collections.emptyMap();
        }
        return IceKeys.resolve(jobWrapper);
    }

    @Override
    public <T> Job<T> pop(String topic) {
        DelayJob delayJob = readyQueue.pop(topic);
        if (delayJob == null) {
            return null;
        }
        Job<T> job = jobPool.getByType(delayJob.getJodId(), new TypeReference<Job<T>>(){});
        // 元数据已经删除，则取下一个
        if (job == null) {
            job = pop(topic);
            return job;
        }

        Job<T> mJob = job;
        // 设置为处理中状态
        mJob.setStatus(JobStatus.RESERVED);
        // 更新延迟时间为TTR
        delayJob.setDelayTime(System.currentTimeMillis() + mJob.getTtr());
        RedisUtil.batchOps((operations) -> {
            jobPool.push(operations, mJob);
            int index = delayBucket.add(operations, delayJob);

            // Record job
            if (jobInspector != null) {
                jobInspector.updateJobInspection(Collections.singletonList(delayJob), index);
            }
        }, redisTemplate);
        return mJob;
    }

    @Override
    public <T> List<Job<T>> pop(String topic, int count) {
        // 获取个数小于1或空队列直接返回
        if (count < 1 || readyQueue.size(topic) == 0) return null;
        // 如果只取1个时，直接使用pop（保证原子性）
        if (count == 1) return Collections.singletonList(pop(topic));

        // 使用SetNX锁住资源，防止多线程并发执行，造成重复消费问题
        boolean hasObtainLock = RedisUtil.setIfAbsent(this.lockKey, props.getTaskPopCountLockTimeoutSeconds().getSeconds(), redisTemplate);
        if (!hasObtainLock) return null;

        List<Job<T>> jobList;
        try {
            List<DelayJob> delayJobList = readyQueue.pop(topic, count);
            if (CollectionUtils.isEmpty(delayJobList)) {
                return null;
            }
            List<String> jobIds = delayJobList.stream().map(DelayJob::getJodId).collect(Collectors.toList());
            jobList = jobPool.getByType(jobIds, new TypeReference<Job<T>>(){}, count);
            // 元数据已经删除，则取下一个
            if (CollectionUtils.isEmpty(jobList)) {
                jobList = pop(topic, count);
                return jobList;
            }
            List<Job<T>> mJobList = jobList;
            for (int i = 0; i < mJobList.size(); i++) {
                Job<T> mJob = mJobList.get(i);
                // 设置为处理中状态
                mJob.setStatus(JobStatus.RESERVED);
                // 更新延迟时间为TTR
                DelayJob delayJob = delayJobList.get(i);
                delayJob.setDelayTime(System.currentTimeMillis() + mJob.getTtr());
            }
            RedisUtil.batchOps((operations) -> {
                jobPool.push(operations, mJobList);
                delayBucket.add(operations, delayJobList);
            }, redisTemplate);
        } finally {
            // 删除Lock
            RedisPolyfill.redisDelete(redisTemplate, this.lockKey);
        }
        return jobList;
    }

    @Override
    public <T> void finish(List<Job<T>> jobs) {
        delete(jobs);

        // Record job
        if (jobInspector != null) {
            jobInspector.finish(jobs.stream().map(Job::getId).collect(Collectors.toList()));
        }
    }

    @Override
    public <T> void finish(RedisOperations<String, String> operations, List<Job<T>> jobs) {
        delete(operations, jobs);
    }

    @Override
    public void finish(Object... jobIds) {
        delete(jobIds);
    }

    @Override
    public void finish(RedisOperations<String, String> operations, Object... jobIds) {
        delete(operations, jobIds);
    }

    @Override
    public <T> void delete(List<Job<T>> jobs) {
        List<String> jobIds = jobs.stream().map(Job::getId).collect(Collectors.toList());
        delete(jobIds.toArray(new Object[]{}));
    }

    @Override
    public <T> void delete(RedisOperations<String, String> operations, List<Job<T>> jobs) {
        List<String> jobIds = jobs.stream().map(Job::getId).collect(Collectors.toList());
        delete(operations, jobIds.toArray(new Object[]{}));
    }

    @Override
    public void delete(Object... jobIds) {
        jobPool.remove(jobIds);
    }

    @Override
    public void delete(RedisOperations<String, String> operations, Object... jobIds) {
        jobPool.remove(operations, jobIds);
    }

    @Override
    public void onApplicationEvent(IceInstanceChangeEvent event) {
        String instanceName = event.getSource().toString();
        this.lockKey = "ice:range_pop_lock:" + instanceName;
    }
}
