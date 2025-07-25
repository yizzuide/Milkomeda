/*
 * Copyright (c) 2025 yizzuide All rights Reserved.
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

package com.github.yizzuide.milkomeda.satellite;

import com.github.yizzuide.milkomeda.util.JSONUtil;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RBatch;
import org.redisson.api.RList;
import org.redisson.api.RStreamAsync;
import org.redisson.api.RedissonClient;
import org.redisson.api.stream.StreamAddArgs;
import org.redisson.client.codec.StringCodec;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Redis batch message publisher.
 *
 * @since 4.0.0
 * @author yizzuide
 * Create at 2025/07/16 11:41
 */
@Slf4j
public class RedisBatchPublisher {

    private final RedissonClient redissonClient;

    public RedisBatchPublisher(RedissonClient redissonClient) {
        this.redissonClient = redissonClient;
    }

    /**
     * Publish batch message in list
     * @param topic topic name
     * @param data message data
     * @param <T> message type
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public <T> void publishBatch(String topic, List<T> data) {
        if (data.isEmpty()) {
            return;
        }
        List<T> messages = data;
        if(!String.class.isAssignableFrom(data.getFirst().getClass())) {
            messages = (List) messages.stream().map(JSONUtil::serialize).toList();
        }

        RList<T> list = redissonClient.getList(topic, StringCodec.INSTANCE);
        list.addAllAsync(messages).whenComplete((ret, throwable) -> {
            if (throwable != null) {
                log.error("Redis发送队列消息异常topic:{}, errorMsg:{}", topic, throwable.getMessage(), throwable);
            } else {
                if (ret) {
                    log.info("Redis发送队列消息成功topic:{}", topic);
                }
            }
        });
    }

    /**
     * Publish batch message in stream
     * @param topic topic name
     * @param data message data
     * @param <T> message type
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public <T> void publishStreamBatch(String topic, List<T> data) {
        if (data.isEmpty()) {
            return;
        }
        List<T> messages = data;
        if(!String.class.isAssignableFrom(data.getFirst().getClass())) {
            messages = (List) messages.stream().map(JSONUtil::serialize).toList();
        }

        int messageCount = messages.size();
        // Redis Stream 方式的消息发送
        RBatch batch = redissonClient.createBatch();
        RStreamAsync<String, String> stream = batch.getStream(topic, StringCodec.INSTANCE);
        // 为所有消息创建批处理
        for (T message : messages) {
            // 每条消息作为一个包含 "payload" 字段的 Map
            Map<String, String> messageMap = new HashMap<>(4);
            messageMap.put("payload", (String) message);
            // 添加到批处理任务
            stream.addAsync(StreamAddArgs.entries(messageMap));
        }
        // 异步执行批处理
        batch.executeAsync().whenComplete((batchResults, throwable) -> {
            if (throwable != null) {
                log.error("Redis发送Stream消息异常, topic:{}, errorMsg:{}", topic, throwable.getMessage(), throwable);
                return;
            }
            int successCount = 0;
            List<?> results = batchResults.getResponses();
            // 统计成功
            for (Object result : results) {
                if (result != null) {
                    successCount++;
                }
            }
            log.info("Redis发送Stream消息[messageId={}]成功, topic:{}，成功条数:{}/{}", results, topic, successCount, messageCount);
        });
    }
}
