package com.github.yizzuide.milkomeda.ice;

import com.github.yizzuide.milkomeda.universe.context.ApplicationContextHolder;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationListener;
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
 * @version 3.0.7
 * Create at 2019/11/16 17:07
 */
public class RedisReadyQueue implements ReadyQueue, InitializingBean, ApplicationListener<IceInstanceChangeEvent> {

    private StringRedisTemplate redisTemplate;

    private String readyQueueKey = "ice:ready_queue";

    public RedisReadyQueue(IceProperties props) {
        if (!IceProperties.DEFAULT_INSTANCE_NAME.equals(props.getInstanceName())) {
            this.readyQueueKey = "ice:ready_queue:" + props.getInstanceName();
        }
    }

    @Override
    public void push(DelayJob delayJob) {
        BoundListOperations<String, String> listOperations = getQueue(delayJob.getTopic());
        listOperations.rightPush(delayJob.toSimple());
    }

    @Override
    public DelayJob pop(String topic) {
        BoundListOperations<String, String> listOperations = getQueue(topic);
        String delayJob = listOperations.leftPop();
        if (null == delayJob) return null;
        return DelayJob.compatibleDecode(delayJob, null);
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
                .map(delayJob -> DelayJob.compatibleDecode(delayJob, null))
                .collect(Collectors.toList());
    }

    @Override
    public long size(String topic) {
        Long size = getQueue(topic).size();
        return size == null ? 0 : size;
    }

    private BoundListOperations<String, String> getQueue(String topic) {
        return redisTemplate.boundListOps(getKey(topic));
    }

    private String getKey(String topic) {
        return this.readyQueueKey + ":" + topic;
    }

    @Override
    public void afterPropertiesSet() {
        redisTemplate = ApplicationContextHolder.get().getBean(StringRedisTemplate.class);
    }

    @Override
    public void onApplicationEvent(IceInstanceChangeEvent event) {
        String instanceName = event.getSource().toString();
        this.readyQueueKey = "ice:ready_queue:" + instanceName;
    }
}
