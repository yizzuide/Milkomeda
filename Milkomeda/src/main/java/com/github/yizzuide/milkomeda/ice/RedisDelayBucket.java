package com.github.yizzuide.milkomeda.ice;

import com.github.yizzuide.milkomeda.universe.context.ApplicationContextHolder;
import com.github.yizzuide.milkomeda.util.JSONUtil;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundZSetOperations;
import org.springframework.data.redis.core.DefaultTypedTuple;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * RedisDelayBucket
 * 当前采用的一个队列任务对应一个Bucket，如果在集群下，需要使用分布式锁（Redis SetNX)保证一个Bucket只在一个线程中处理
 *
 * @author yizzuide
 * @since 1.15.0
 * Create at 2019/11/16 16:17
 */
public class RedisDelayBucket implements DelayBucket, InitializingBean {

    private StringRedisTemplate redisTemplate;

    private static AtomicInteger index = new AtomicInteger(0);

    private List<String> bucketNames = new ArrayList<>();

    @Autowired
    private IceProperties props;

    @Override
    public void add(DelayJob delayJob) {
        String bucketName = getCurrentBucketName();
        BoundZSetOperations<String, String> bucket = getBucket(bucketName);
        bucket.add(JSONUtil.serialize(delayJob), delayJob.getDelayTime());
    }

    @Override
    public void add(List<DelayJob> delayJobs) {
        String bucketName = getCurrentBucketName();
        BoundZSetOperations<String, String> bucket = getBucket(bucketName);
        Set<ZSetOperations.TypedTuple<String>> delayJobSet = delayJobs.stream()
                .map(delayJob -> new DefaultTypedTuple<>(JSONUtil.serialize(delayJob), (double) delayJob.getDelayTime()))
                .collect(Collectors.toSet());
        bucket.add(delayJobSet);
    }

    @SuppressWarnings("unchecked")
    @Override
    public DelayJob poll(Integer index) {
        String name = bucketNames.get(index);
        BoundZSetOperations<String, String> bucket = getBucket(name);
        // 升序查第一个（最上面的是延迟/TTR过期的）
        Set<ZSetOperations.TypedTuple<String>> set = bucket.rangeWithScores(0, 1);
        if (CollectionUtils.isEmpty(set)) {
            return null;
        }
        ZSetOperations.TypedTuple<String> typedTuple = set.toArray(new ZSetOperations.TypedTuple[]{})[0];
        return JSONUtil.parse(typedTuple.getValue(), DelayJob.class);
    }

    @Override
    public void remove(Integer index, DelayJob delayJob) {
        String name = bucketNames.get(index);
        BoundZSetOperations bucket = getBucket(name);
        bucket.remove(JSONUtil.serialize(delayJob));
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
    private String getCurrentBucketName() {
        int thisIndex = index.getAndIncrement();
        return bucketNames.get(thisIndex % props.getDelayBucketCount());
    }

    @Override
    public void afterPropertiesSet() {
        redisTemplate = ApplicationContextHolder.get().getBean(StringRedisTemplate.class);
        for (int i = 0; i < props.getDelayBucketCount(); i++) {
            bucketNames.add("ice:bucket" + i);
        }
    }
}
