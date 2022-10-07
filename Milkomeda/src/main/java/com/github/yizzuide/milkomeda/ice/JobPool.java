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
import org.springframework.data.redis.core.RedisOperations;

import java.util.List;

/**
 * JobPool
 *
 * @author yizzuide
 * @since 1.15.0
 * @version 3.12.0
 * <br />
 * Create at 2019/11/16 15:42
 */
public interface JobPool {

    /**
     * 添加任务
     * @param operations Pipelined操作
     * @param job Job
     * @since 3.12.0
     */
    @SuppressWarnings("rawtypes")
    void push(RedisOperations<String, String> operations, Job job);

    /**
     * 添加多个任务
     * @param operations Pipelined操作
     * @param  jobs List
     * @param <T> 任务类型
     * @since 3.12.0
     */
    <T> void push(RedisOperations<String, String> operations, List<Job<T>> jobs);

    /**
     * 任务是否存在
     * @param jobId 任务id
     * @return 是否存在
     * @since 3.0.0
     */
    boolean exists(String jobId);

    /**
     * 获取任务
     * @param jobId 任务id
     * @return Job
     */
    @SuppressWarnings("rawtypes")
    Job get(String jobId);

    /**
     * 获取任务
     * @param jobId 任务id
     * @param typeReference TypeReference
     * @param <T> 实体类型
     * @return Job
     */
    <T> Job<T> getByType(String jobId, TypeReference<Job<T>> typeReference);

    /**
     * 批量获取任务
     * @param jobIds 任务id列表
     * @param typeReference TypeReference
     * @param count 批量数
     * @param <T> 实体类型
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

    /**
     * 移除任务
     * @param operations Pipelined操作
     * @param jobIds 任务id
     * @since 3.12.0
     */
    void remove(RedisOperations<String, String> operations, Object... jobIds);
}
