package com.github.yizzuide.milkomeda.ice;

import com.fasterxml.jackson.core.type.TypeReference;

import java.util.List;

/**
 * JobPool
 *
 * @author yizzuide
 * @since 1.15.0
 * Create at 2019/11/16 15:42
 */
public interface JobPool {
    /**
     * 添加任务
     * @param job Job
     */
    void push(Job job);

    /**
     * 添加多个任务
     * @param  jobs List
     */
    <T> void push(List<Job<T>> jobs);

    /**
     * 获取任务
     * @param jobId 任务id
     * @return Job
     */
    Job get(String jobId);

    /**
     * 获取任务
     * @param jobId 任务id
     * @param typeReference TypeReference
     * @return Job
     */
    <T> Job<T> getByType(String jobId, TypeReference<Job<T>> typeReference);

    /**
     * 批量获取任务
     * @param jobIds 任务id列表
     * @param typeReference TypeReference
     * @param count 批量数
     * @return Job
     */
    <T> List<Job<T>> getByType(List<String> jobIds, TypeReference<Job<T>> typeReference, int count);

    /**
     * 批量获取任务
     * @param jobIds 任务id列表
     * @param typeReference TypeReference
     * @param count 批量数
     * @return Job
     */
    List<Job<String>> getByStringType(List<String> jobIds, TypeReference<Job<String>> typeReference, int count);

    /**
     * 移除任务
     * @param jobIds 任务id
     */
    void remove(Object... jobIds);
}
