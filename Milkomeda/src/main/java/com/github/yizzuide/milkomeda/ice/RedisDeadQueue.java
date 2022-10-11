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
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.event.EventListener;
import org.springframework.data.redis.core.BoundSetOperations;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * RedisDeadQueue
 *
 * @author yizzuide
 * @since 3.0.8
 * @version 3.14.0
 * <br>
 * Create at 2020/04/17 00:51
 */
public class RedisDeadQueue implements DeadQueue, InitializingBean {

    private StringRedisTemplate redisTemplate;

    private String deadQueueKey = IceKeys.DEAD_QUEUE_KEY_PREFIX;

    public RedisDeadQueue(IceProperties props) {
        if (!IceProperties.DEFAULT_INSTANCE_NAME.equals(props.getInstanceName())) {
            this.deadQueueKey = IceKeys.DEAD_QUEUE_KEY_PREFIX + ":" + props.getInstanceName();
        }
    }

    @Override
    public void add(RedisOperations<String, String> operations, DelayJob delayJob) {
        operations.boundSetOps(this.deadQueueKey).add(JSONUtil.serialize(delayJob));
    }

    public void remove(DelayJob delayJob) {
        getDeadQueue(this.deadQueueKey).remove(JSONUtil.serialize(delayJob));
    }

    @Override
    public DelayJob pop() {
        String delayJob = getDeadQueue(this.deadQueueKey).pop();
        if (delayJob == null) {
            return null;
        }
        return JSONUtil.parse(delayJob, DelayJob.class);
    }

    @Override
    public List<DelayJob> pop(long count) {
        BoundSetOperations<String, String> deadQueue = getDeadQueue(this.deadQueueKey);
        Set<String> members = deadQueue.distinctRandomMembers(count);
        if (CollectionUtils.isEmpty(members)) {
            return null;
        }
        deadQueue.remove(members.toArray());
        return members.stream().map(s -> JSONUtil.parse(s, DelayJob.class)).collect(Collectors.toList());
    }

    @Override
    public List<DelayJob> popALL() {
        Set<String> members = getDeadQueue(this.deadQueueKey).members();
        if (CollectionUtils.isEmpty(members)) {
            return null;
        }
        return members.stream().map(s -> JSONUtil.parse(s, DelayJob.class)).collect(Collectors.toList());
    }

    private BoundSetOperations<String, String> getDeadQueue(String key) {
        return redisTemplate.boundSetOps(key);
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        redisTemplate = ApplicationContextHolder.get().getBean(StringRedisTemplate.class);
    }

    @EventListener
    public void onApplicationEvent(IceInstanceChangeEvent event) {
        String instanceName = event.getSource().toString();
        this.deadQueueKey = IceKeys.DEAD_QUEUE_KEY_PREFIX + ":" + instanceName;
    }
}
