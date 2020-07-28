package com.github.yizzuide.milkomeda.ice;

import org.springframework.data.redis.core.RedisOperations;

import java.util.List;

/**
 * DelayBucket
 *
 * @author yizzuide
 * @since 1.15.0
 * @version 3.12.0
 * Create at 2019/11/16 16:03
 */
public interface DelayBucket {

    /**
     * 放入延时任务
     * @param operations Pipelined操作
     * @param delayJob DelayJob
     * @since 3.12.0
     */
    void add(RedisOperations<String, String> operations, DelayJob delayJob);

    /**
     * 批量放入延时任务
     * @param delayJobs List
     */
    void add(List<DelayJob> delayJobs);

    /**
     * 批量放入延时任务
     * @param operations Pipelined操作
     * @param delayJobs List
     * @since 3.12.0
     */
    void add(RedisOperations<String, String> operations, List<DelayJob> delayJobs);

    /**
     * 获得最新的延期任务
     *
     * @param index 指定的桶
     * @return DelayJob
     */
    DelayJob poll(Integer index);

    /**
     * 移除延时任务
     * @param index     指定的桶
     * @param delayJob  DelayJob
     */
    void remove(Integer index, DelayJob delayJob);
    /**
     * 移除延时任务
     * @param operations Pipelined操作
     * @param index     指定的桶
     * @param delayJob  DelayJob
     * @since 3.12.0
     */
    void remove(RedisOperations<String, String> operations, Integer index, DelayJob delayJob);
}
