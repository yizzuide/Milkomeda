package com.github.yizzuide.milkomeda.ice;

import com.fasterxml.jackson.core.type.TypeReference;
import com.github.yizzuide.milkomeda.universe.polyfill.RedisPolyfill;
import com.github.yizzuide.milkomeda.util.RedisUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.util.CollectionUtils;

import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * RedisIce
 * 基于Redis的延迟队列实现
 *
 * @author yizzuide
 * @since 1.15.0
 * @version 3.0.9
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
    private StringRedisTemplate redisTemplate;

    private IceProperties props;

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
        add(job, true);
    }

    @SuppressWarnings("rawtypes")
    @Override
    public void add(Job job, boolean mergeIdWithTopic) {
        if (mergeIdWithTopic) {
            job.setId(job.getTopic() + "-" + job.getId());
        }
        if (jobPool.exists(job.getId())) {
            return;
        }
        job.setStatus(JobStatus.DELAY);
        RedisUtil.batchOps(() -> {
            jobPool.push(job);
            delayBucket.add(new DelayJob(job));
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
        RedisUtil.batchOps(() -> {
            // 设置为处理中状态
            mJob.setStatus(JobStatus.RESERVED);
            // 更新延迟时间为TTR
            delayJob.setDelayTime(System.currentTimeMillis() + mJob.getTtr());
            jobPool.push(mJob);
            delayBucket.add(delayJob);
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
            RedisUtil.batchOps(() -> {
                for (int i = 0; i < mJobList.size(); i++) {
                    Job<T> mJob = mJobList.get(i);
                    // 设置为处理中状态
                    mJob.setStatus(JobStatus.RESERVED);
                    // 更新延迟时间为TTR
                    DelayJob delayJob = delayJobList.get(i);
                    delayJob.setDelayTime(System.currentTimeMillis() + mJob.getTtr());
                }
                jobPool.push(mJobList);
                delayBucket.add(delayJobList);
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
    }

    @Override
    public void finish(Object... jobIds) {
        delete(jobIds);
    }

    @Override
    public <T> void delete(List<Job<T>> jobs) {
        List<String> jobIds = jobs.stream().map(Job::getId).collect(Collectors.toList());
        delete(jobIds.toArray(new Object[]{}));
    }

    @Override
    public void delete(Object... jobIds) {
        jobPool.remove(jobIds);
    }

    @Override
    public void onApplicationEvent(IceInstanceChangeEvent event) {
        String instanceName = event.getSource().toString();
        this.lockKey = "ice:range_pop_lock:" + instanceName;
    }
}
