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

import com.github.yizzuide.milkomeda.ice.inspector.JobInspectPage;
import com.github.yizzuide.milkomeda.ice.inspector.JobWrapper;
import com.github.yizzuide.milkomeda.universe.lang.Tuple;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.redis.core.RedisOperations;

import java.time.Duration;
import java.util.List;
import java.util.Map;

/**
 * Ice
 * 最外层接口
 *
 * @author yizzuide
 * @since 1.15.0
 * @version 3.14.0
 * <br>
 * Create at 2019/11/16 15:11
 */
public interface Ice {
    /**
     * 添加延迟任务（job的id会与topic合并）
     * @param job   Job
     * @return true if add success
     */
    @SuppressWarnings("rawtypes")
    boolean add(Job job);

    /**
     * 添加延迟任务
     * @param job               Job
     * @param mergeIdWithTopic  是否把Job的topic合并进id（一般是建议合并，因为延迟任务会有topic流转再入队的情况）
     * @param replaceWhenExists 当存在时替换
     * @return true if add success
     * @since 3.0.9
     */
    @SuppressWarnings("rawtypes")
    boolean add(Job job, boolean mergeIdWithTopic, boolean replaceWhenExists);

    /**
     * 添加延迟任务（job的id会与topic合并）
     * @param id    任务id
     * @param topic 任务分组
     * @param body  业务数据
     * @param delay 延迟时间
     * @param <T>   业务数据类型
     * @return true if add success
     * @since 3.0.0
     */
    <T> boolean add(String id, String topic, T body, Duration delay);

    /**
     * 添加延迟任务（job的id会与topic合并）
     * @param id    任务id
     * @param topic 任务分组
     * @param body  业务数据
     * @param delay 延迟时间ms
     * @param <T>   业务数据类型
     * @return true if add success
     * @since 3.0.0
     */
    <T> boolean add(String id, String topic, T body, long delay);

    /**
     * 构建延迟任务
     * @param id    任务id
     * @param topic 任务分组
     * @param body  业务数据
     * @param delay 延迟时间
     * @param <T>   业务数据类型
     * @return Job
     * @since 3.0.9
     */
    <T> Job<T> build(String id, String topic, T body, Duration delay);

    /**
     * 构建延迟任务
     * @param id    任务id
     * @param topic 任务分组
     * @param body  业务数据
     * @param delay 延迟时间ms
     * @param <T>   业务数据类型
     * @return Job
     * @since 3.0.9
     */
    <T> Job<T> build(String id, String topic, T body, long delay);

    /**
     * Re-push job to job pool.
     * @param jobId job id
     * @param topic job topic
     * @return ture if push success
     * @since 3.14.0
     */
    boolean rePushJob(String jobId, String topic);

    /**
     * Get all job inspect info list.
     * @param start page start index
     * @param size size of per page
     * @param order sorting of corresponding column, 1 is asc and -1 is desc
     * @return job Inspection page data
     * @since 3.14.0
     */
    JobInspectPage getJobInspectPage(int start, int size, int order);

    /**
     * Get job inspect info with topic and job id.
     * @param topic job topic
     * @param jobId job id
     * @return  job inspection info
     * @since 3.14.0
     */
    JobWrapper getJobInspectInfo(String topic, String jobId);

    /**
     * Get job info in job pool.
     * @param jobId job id
     * @param topic job topic
     * @return  Job
     * @since 3.14.0
     */
    Job<?> getJobDetail(String jobId, String topic);

    /**
     * Get cache keys.
     * @param jobId job id
     * @param topic job topic
     * @return key map
     * @since 3.14.0
     */
    Map<String, String> getCacheKey(String jobId, String topic);

    /**
     * Pull number of jobs.
     * @param topic job topic
     * @param count pull size
     * @param <T>   job list type
     * @return  job list
     */
    <T> List<Job<T>> pull(String topic, Integer count);

    /**
     * 取出待处理任务
     * @param topic 任务分组
     * @param <T>   业务数据
     * @return  Job
     */
    <T> Job<T> pop(String topic);

    /**
     * 批量取出待处理任务
     * @param topic 任务分组
     * @param count 批量数
     * @param <T>   业务数据
     * @return List
     */
    <T> List<Job<T>> pop(String topic, int count);

    /**
     * 完成任务
     * @param jobs    任务列表
     * @param <T>   业务数据
     */
    <T> void finish(List<Job<T>> jobs);

    /**
     * finish job list.
     * @param jobIds job id list
     */
    void finish(Object... jobIds);

    /**
     * 删除任务
     * @param jobs    任务列表
     * @param <T>   业务数据
     */
    <T> void delete(List<Job<T>> jobs);

    /**
     * 删除任务
     * @param operations Pipelined操作
     * @param jobs    任务列表
     * @param <T>   业务数据
     * @since 3.12.0
     */
    <T> void delete(RedisOperations<String, String> operations, List<Job<T>> jobs);

    /**
     * 删除任务
     * @param jobIds    任务id列表
     */
    void delete(Object... jobIds);

    /**
     * 删除任务
     * @param operations Pipelined操作
     * @param jobIds    任务id列表
     * @since 3.12.0
     */
    void delete(RedisOperations<String, String> operations, Object... jobIds);

    /**
     * Extract job id.
     * @param mixId merged topic with job id
     * @return pure job id
     * @since 3.14.0
     */
    @NotNull
    static String getId(String mixId) {
        if (!mixId.contains(IceProperties.MERGE_ID_SEPARATOR)) {
            return mixId;
        }
        return deconstruct(mixId).getT2();
    }

    /**
     * Deconstructing topic and job id within mixId.
     * @param mixId merged topic with job id
     * @return  tuple(topic, jobId)
     * @since 3.14.0
     */
    @NotNull
    static Tuple<String, String> deconstruct(String mixId) {
        if (!mixId.contains(IceProperties.MERGE_ID_SEPARATOR)) {
            throw new IllegalArgumentException("Ice deconstruct job error with mixId: " + mixId);
        }
        String[] parts = mixId.split(IceProperties.MERGE_ID_SEPARATOR);
        return Tuple.build(parts[0], parts[1]);
    }

    /**
     * Mixins job id  with topic.
     * @param jobId pure job id
     * @param topic job topic
     * @return  merged topic with job id
     * @since 3.14.0
     */
    @NotNull
    static String mergeId(Object jobId, String topic) {
        if (jobId.toString().contains(IceProperties.MERGE_ID_SEPARATOR)) {
            return jobId.toString();
        }
        return topic + IceProperties.MERGE_ID_SEPARATOR + jobId;
    }
}
