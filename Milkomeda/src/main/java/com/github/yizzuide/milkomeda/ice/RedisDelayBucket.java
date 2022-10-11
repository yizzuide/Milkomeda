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

import com.github.yizzuide.milkomeda.universe.context.ApplicationContextHolder;
import com.github.yizzuide.milkomeda.util.JSONUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationListener;
import org.springframework.data.redis.core.*;
import org.springframework.data.util.Pair;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * RedisDelayBucket
 * 延迟桶
 *
 * @author yizzuide
 * @since 1.15.0
 * @version 3.8.0
 * @version 3.14.0
 * <br>
 * Create at 2019/11/16 16:17
 */
@Slf4j
public class RedisDelayBucket implements DelayBucket, InitializingBean, ApplicationListener<IceInstanceChangeEvent> {

    private final IceProperties props;

    private StringRedisTemplate redisTemplate;

    private final List<String> bucketNames = new ArrayList<>();

    private static final AtomicInteger index = new AtomicInteger(0);

    // 默认最大桶大小
    public static final int DEFAULT_MAX_BUCKET_SIZE = 100;

    public RedisDelayBucket(IceProperties props) {
        this.props = props;
        for (int i = 0; i < props.getDelayBucketCount(); i++) {
            if (IceProperties.DEFAULT_INSTANCE_NAME.equals(props.getInstanceName())) {
                bucketNames.add(IceKeys.DELAY_BUCKET_KEY_PREFIX + i);
            } else {
                bucketNames.add(IceKeys.DELAY_BUCKET_KEY_PREFIX + i + ":" + props.getInstanceName());
            }
        }
    }

    @Override
    public Integer add(RedisOperations<String, String> operations, DelayJob delayJob) {
        Pair<Integer, String> bucketIndexPair = getCurrentBucketName();
        String bucketName = bucketIndexPair.getSecond();
        operations.boundZSetOps(bucketName).add(delayJob.toSimple(), delayJob.getDelayTime());
        return bucketIndexPair.getFirst();
    }

    @Override
    public void add(List<DelayJob> delayJobs) {
        Pair<Integer, String> bucketIndexPair = getCurrentBucketName();
        String bucketName = bucketIndexPair.getSecond();
        Set<ZSetOperations.TypedTuple<String>> delayJobSet = delayJobs.stream()
                .map(delayJob -> {
                    delayJob.updateDelayTime();
                    return new DefaultTypedTuple<>(delayJob.toSimple(), (double) delayJob.getDelayTime());
                })
                .collect(Collectors.toSet());
        getBucket(bucketName).add(delayJobSet);

        // Record job
        if (IceHolder.getJobInspector() != null) {
            IceHolder.getJobInspector().updateJobInspection(delayJobs, bucketIndexPair.getFirst());
        }

    }

    @Override
    public void add(RedisOperations<String, String> operations, List<DelayJob> delayJobs) {
        Pair<Integer, String> bucketIndexPair = getCurrentBucketName();
        String bucketName = bucketIndexPair.getSecond();
        Set<ZSetOperations.TypedTuple<String>> delayJobSet = delayJobs.stream()
                .map(delayJob -> new DefaultTypedTuple<>(delayJob.toSimple(), (double) delayJob.getDelayTime()))
                .collect(Collectors.toSet());
        operations.boundZSetOps(bucketName).add(delayJobSet);

        // Record job
        if (IceHolder.getJobInspector() != null) {
            IceHolder.getJobInspector().updateJobInspection(delayJobs, bucketIndexPair.getFirst());
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public DelayJob poll(Integer index) {
        String name = bucketNames.get(index);
        //log.info("Ice正在处理第{}个延迟分桶: {}", index, name);
        BoundZSetOperations<String, String> bucket = getBucket(name);
        // 升序查第一个（最上面的是延迟/TTR过期的）
        Set<ZSetOperations.TypedTuple<String>> set = bucket.rangeWithScores(0, 1);
        if (CollectionUtils.isEmpty(set)) {
            return null;
        }
        ZSetOperations.TypedTuple<String> typedTuple = set.toArray(new ZSetOperations.TypedTuple[]{})[0];
        if (typedTuple.getValue() == null) {
            return null;
        }
        return DelayJob.compatibleDecode(typedTuple.getValue(), typedTuple.getScore());
    }

    @Override
    public void remove(Integer index, DelayJob delayJob) {
        String name = bucketNames.get(index);
        BoundZSetOperations<String, String> bucket = getBucket(name);
        // 优化后的方式删除
        if (delayJob.isUsedSimple()) {
            bucket.remove(delayJob.toSimple());
            return;
        }
        // 兼容旧方式序列化删除
        bucket.remove(JSONUtil.serialize(delayJob));
    }

    @Override
    public void remove(RedisOperations<String, String> operations, Integer index, DelayJob delayJob) {
        String name = bucketNames.get(index);
        // 优化后的方式删除
        if (delayJob.isUsedSimple()) {
            operations.boundZSetOps(name).remove(delayJob.toSimple());
            return;
        }
        // 兼容旧方式序列化删除
        operations.boundZSetOps(name).remove(JSONUtil.serialize(delayJob));
    }

    /**
     * 获得桶的ZSet
     *
     * @param bucketName 桶名
     * @return BoundZSetOperations
     */
    private BoundZSetOperations<String, String> getBucket(String bucketName) {
        return redisTemplate.boundZSetOps(bucketName);
    }

    /**
     * 获得桶的名称
     *
     * @return BucketName
     */
    private Pair<Integer, String> getCurrentBucketName() {
        int thisIndex = index.getAndIncrement() % DEFAULT_MAX_BUCKET_SIZE;
        String bucketName = bucketNames.get(thisIndex % props.getDelayBucketCount());
        return Pair.of(thisIndex, bucketName);
    }

    @Override
    public void afterPropertiesSet() {
        redisTemplate = ApplicationContextHolder.get().getBean(StringRedisTemplate.class);
    }

    @Override
    public void onApplicationEvent(IceInstanceChangeEvent event) {
        String instanceName = event.getSource().toString();
        bucketNames.clear();
        for (int i = 0; i < props.getDelayBucketCount(); i++) {
            bucketNames.add(IceKeys.DELAY_BUCKET_KEY_PREFIX+ i + ":" + instanceName);
        }
    }
}
