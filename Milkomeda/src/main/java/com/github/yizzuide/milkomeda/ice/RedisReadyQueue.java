package com.github.yizzuide.milkomeda.ice;

import com.github.yizzuide.milkomeda.universe.context.ApplicationContextHolder;
import com.github.yizzuide.milkomeda.util.JSONUtil;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.data.redis.core.BoundListOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.stream.Collectors;

/**
 * RedisReadyQueue
 *
 * @author yizzuide
 * @since 1.15.0
 * Create at 2019/11/16 17:07
 */
public class RedisReadyQueue implements ReadyQueue, InitializingBean {
    private static final String PREFIX_NAME = "ice:ready_queue";

    private StringRedisTemplate redisTemplate;

    @Override
    public void push(DelayJob delayJob) {
        BoundListOperations<String, String> listOperations = getQueue(delayJob.getTopic());
        listOperations.rightPush(JSONUtil.serialize(delayJob));
    }

    @Override
    public DelayJob pop(String topic) {
        BoundListOperations<String, String> listOperations = getQueue(topic);
        String delayJob = listOperations.leftPop();
        if (null == delayJob) return null;
        return JSONUtil.parse(delayJob, DelayJob.class);
    }

    @Override
    public List<DelayJob> pop(String topic, int count) {
        // 获取区间
        List<String> delayJobOrigList = getQueue(topic).range(0, count);
        if (CollectionUtils.isEmpty(delayJobOrigList)) {
            return null;
        }
        // 删除区间
        getQueue(topic).trim(count + 1, -1);
        return delayJobOrigList.stream()
                .map(delayJob -> JSONUtil.parse(delayJob, DelayJob.class))
                .collect(Collectors.toList());
    }

    private BoundListOperations<String, String> getQueue(String topic) {
        return redisTemplate.boundListOps(getKey(topic));
    }

    private String getKey(String topic) {
        return PREFIX_NAME + ":" + topic;
    }

    @Override
    public void afterPropertiesSet() {
        redisTemplate = ApplicationContextHolder.get().getBean(StringRedisTemplate.class);
    }
}
