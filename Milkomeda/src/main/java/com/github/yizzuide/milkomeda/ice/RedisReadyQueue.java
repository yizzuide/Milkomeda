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
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationListener;
import org.springframework.data.redis.core.BoundListOperations;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.stream.Collectors;

/**
 * RedisReadyQueue
 *
 * @author yizzuide
 * @since 1.15.0
 * @version 3.12.0
 * Create at 2019/11/16 17:07
 */
public class RedisReadyQueue implements ReadyQueue, InitializingBean, ApplicationListener<IceInstanceChangeEvent> {

    private StringRedisTemplate redisTemplate;

    private String readyQueueKey = IceKeys.READY_QUEUE_KEY_PREFIX;

    public RedisReadyQueue(IceProperties props) {
        if (!IceProperties.DEFAULT_INSTANCE_NAME.equals(props.getInstanceName())) {
            this.readyQueueKey = IceKeys.READY_QUEUE_KEY_PREFIX + ":" + props.getInstanceName();
        }
    }

    @Override
    public void push(RedisOperations<String, String> operations, DelayJob delayJob) {
        operations.boundListOps(getKey(delayJob.getTopic())).rightPush(delayJob.toSimple());
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
        this.readyQueueKey = IceKeys.READY_QUEUE_KEY_PREFIX + ":" + instanceName;
    }
}
