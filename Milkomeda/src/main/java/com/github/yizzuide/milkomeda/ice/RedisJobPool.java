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
import com.github.yizzuide.milkomeda.universe.context.ApplicationContextHolder;
import com.github.yizzuide.milkomeda.util.JSONUtil;
import com.github.yizzuide.milkomeda.util.Strings;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationListener;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * RedisJobPool
 *
 * @author yizzuide
 * @since 1.15.0
 * @version 3.12.0
 * <br>
 * Create at 2019/11/16 15:45
 */
public class RedisJobPool implements JobPool, InitializingBean, ApplicationListener<IceInstanceChangeEvent> {

    private StringRedisTemplate redisTemplate;

    private String jobPoolKey = IceKeys.JOB_POOL_KEY_PREFIX;

    public RedisJobPool(IceProperties props) {
        if (!IceProperties.DEFAULT_INSTANCE_NAME.equals(props.getInstanceName())) {
            this.jobPoolKey = IceKeys.JOB_POOL_KEY_PREFIX + ":" + props.getInstanceName();
        }
    }

    private BoundHashOperations<String, String, String> getPool () {
        return redisTemplate.boundHashOps(jobPoolKey);
    }

    @SuppressWarnings("rawtypes")
    @Override
    public void push(RedisOperations<String, String> operations, Job job) {
        operations.boundHashOps(jobPoolKey).put(job.getId(), JSONUtil.serialize(job));
    }

    @Override
    public <T> void push(RedisOperations<String, String> operations, List<Job<T>> jobs) {
        operations.boundHashOps(jobPoolKey).putAll(jobs.stream().collect(Collectors.toMap(Job::getId, JSONUtil::serialize)));
    }

    @Override
    public boolean exists(String jobId) {
        String job = getPool().get(jobId);
        return !Strings.isEmpty(job);
    }

    @SuppressWarnings("rawtypes")
    @Override
    public Job get(String jobId) {
        String job = getPool().get(jobId);
        if (null == job) return null;
        return JSONUtil.parse(job, Job.class);
    }

    @Override
    public <T> Job<T> getByType(String jobId, TypeReference<Job<T>> typeReference) {
        String job = getPool().get(jobId);
        if (null == job) return null;
        return JSONUtil.nativeRead(job, typeReference);
    }

    @Override
    public <T> List<Job<T>> getByType(List<String> jobIds, TypeReference<Job<T>> typeReference, int count) {
        List<String> jobOrigList = getPool().multiGet(jobIds);
        if (CollectionUtils.isEmpty(jobOrigList)) {
            return null;
        }

        List<Job<T>> jobList = new ArrayList<>();
        for (String job : jobOrigList) {
            if (null == job) {
                continue;
            }
            jobList.add(JSONUtil.nativeRead(job, typeReference));
        }
        return jobList;
    }

    @Override
    public List<Job<String>> getByStringType(List<String> jobIds, TypeReference<Job<String>> typeReference, int count) {
        return getByType(jobIds, typeReference, count);
    }

    @Override
    public void remove(Object... jobIds) {
        getPool().delete(jobIds);
    }

    @Override
    public void remove(RedisOperations<String, String> operations, Object... jobIds) {
        operations.boundHashOps(jobPoolKey).delete(jobIds);
    }

    @Override
    public void afterPropertiesSet() {
        redisTemplate = ApplicationContextHolder.get().getBean(StringRedisTemplate.class);
    }

    @Override
    public void onApplicationEvent(IceInstanceChangeEvent event) {
        String instanceName = event.getSource().toString();
        jobPoolKey = IceKeys.JOB_POOL_KEY_PREFIX + ":" + instanceName;
    }
}
