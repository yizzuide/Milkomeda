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

import com.github.yizzuide.milkomeda.universe.context.SpringContext;
import com.github.yizzuide.milkomeda.universe.extend.loader.LuaLoader;
import com.github.yizzuide.milkomeda.universe.metadata.HandlerMetaData;
import com.github.yizzuide.milkomeda.util.JSONUtil;
import jakarta.annotation.Nonnull;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.PendingEntry;
import org.redisson.api.RStream;
import org.redisson.api.RedissonClient;
import org.redisson.api.StreamMessageId;
import org.redisson.api.stream.StreamCreateGroupArgs;
import org.redisson.api.stream.StreamReadArgs;
import org.redisson.api.stream.StreamReadGroupArgs;
import org.redisson.client.RedisBusyException;
import org.redisson.client.codec.StringCodec;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DigestUtils;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.lang.NonNull;
import org.springframework.scheduling.TaskScheduler;

import java.io.IOException;
import java.time.Duration;
import java.util.*;

/**
 * Redis message queue scheduling processor.
 *
 * @since 4.0.0
 * @author yizzuide
 * Create at 2025/07/16 13:41
 */
@Slf4j
public class RedisMessageProcessor implements ApplicationListener<ContextRefreshedEvent>, LuaLoader {

    private final RedissonClient redissonClient;

    private final TaskScheduler taskScheduler;

    private final RedisTemplate<String, String> stringRedisTemplate;

    public static Map<String, List<HandlerMetaData>> topicHandlerMap = new HashMap<>();

    public static final String  ATTR_STREAM = "ATTR_STREAM";

    @Value("${spring.application.name}")
    private String appName;

    @Setter
    private String[] luaScripts;

    // 消费者名称（每个实例唯一）
    private String consumerName;

    public RedisMessageProcessor(RedissonClient redissonClient, TaskScheduler taskScheduler, RedisTemplate<String, String> stringRedisTemplate) {
        this.redissonClient = redissonClient;
        this.taskScheduler = taskScheduler;
        this.stringRedisTemplate = stringRedisTemplate;
    }

    @Override
    public void onApplicationEvent(@NonNull ContextRefreshedEvent event) {
        topicHandlerMap = SpringContext.getHandlerMetaData(RedisMessageHandler.class, RedisMessageListener.class, (annotation, handlerAnnotation, metaData) -> {
            RedisMessageListener listener = (RedisMessageListener) annotation;
            if (listener.stream()) {
                RStream<String, String> stream = redissonClient.getStream(listener.topic(), StringCodec.INSTANCE);
                // 确保消费组存在（忽略已存在的异常）
                try {
                    stream.createGroup(StreamCreateGroupArgs.name(listener.group()).id(StreamMessageId.ALL).makeStream());
                } catch (RedisBusyException ignore) {
                }
                Map<String, Object> attrs = new HashMap<>(4);
                attrs.put(ATTR_STREAM, stream);
                metaData.setAttributes(attrs);
            }
            return listener.topic();
        }, true);

        if (topicHandlerMap.isEmpty()) {
            return;
        }

        try {
            load();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        // 初始化消费者名称
        this.consumerName = appName + "-" + UUID.randomUUID().toString().substring(0, 8);
        taskScheduler.scheduleWithFixedDelay(this::processBatches, Duration.ofMillis(100));
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public void processBatches() {
        topicHandlerMap.forEach((topic, handlers) -> {
            HandlerMetaData handlerMetaData = handlers.getFirst();
            int batchSize = ((RedisMessageListener) handlerMetaData.getAnnotation()).batchSize();
            boolean useStream = ((RedisMessageListener) handlerMetaData.getAnnotation()).stream();
            String group = ((RedisMessageListener) handlerMetaData.getAnnotation()).group();
            int timeout = ((RedisMessageListener) handlerMetaData.getAnnotation()).timeout();

            if (!useStream) {
                List<String> messages = popBatch(topic, batchSize, timeout);
                if (messages == null || messages.isEmpty()) {
                    return;
                }
                invokeMethod(handlerMetaData, messages);
                return;
            }

            RStream<String, String> stream = (RStream) handlerMetaData.getAttributes().get(ATTR_STREAM);
            Map<StreamMessageId, Map<String, String>> messageMap = popStreamBatch(stream, topic, batchSize, group, timeout);
            if (messageMap.isEmpty()) {
                return;
            }
            StreamMessageId[] ids = messageMap.keySet().toArray(new StreamMessageId[0]);
            log.info("Redis接收Stream消息[messageId={}]成功", Arrays.asList(ids));
            // 提取消息体（约定使用"payload"字段）
            List<String> messages = messageMap.values().stream()
                    .map(msgMap -> msgMap.get("payload"))
                    .filter(Objects::nonNull)
                    .toList();
            invokeMethod(handlerMetaData, messages);
            // 批处理Acknowledgement
            stream.ack(group, ids);
        });
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private List<String> popBatch(String topic, int batchSize, int timeout) {
        RedisScript<List<String>> redisScript = new RedisScript<>() {
            @Nonnull
            @Override
            public String getSha1() { return DigestUtils.sha1DigestAsHex(getScriptAsString()); }
            @Override
            public Class<List<String>> getResultType() { return (Class) List.class; }
            @Nonnull
            @Override
            public String getScriptAsString() { return luaScripts[0]; }
        };
        return stringRedisTemplate.execute(redisScript, Collections.singletonList(topic), String.valueOf(batchSize), String.valueOf(timeout));
    }


    private Map<StreamMessageId, Map<String, String>> popStreamBatch(RStream<String, String> stream, String topic, int batchSize, String group, int timeout) {
        // Redis Stream实现（批量分组消费）
        try {
            // 优先消费Pending消息
            Map<StreamMessageId, Map<String, String>> messageMap = new LinkedHashMap<>();
            addPendingMessages(stream, group, batchSize, messageMap);

            // 补充消费新消息
            int remaining = batchSize - messageMap.size();
            if (remaining > 0) {
                StreamReadGroupArgs streamReadGroupArgs = StreamReadGroupArgs.neverDelivered().count(remaining);
                if (timeout > 0) {
                    streamReadGroupArgs.timeout(Duration.ofMillis(timeout));
                }
                Map<StreamMessageId, Map<String, String>> newMessages = stream.readGroup(
                        group,
                        consumerName,
                        streamReadGroupArgs);
                if (newMessages != null) {
                    messageMap.putAll(newMessages);
                }
            }

            if (messageMap.isEmpty()) {
                return Collections.emptyMap();
            }
            return messageMap;
        } catch (Exception e) {
            log.error("Redis stream error for topic: {}", topic, e);
            return Collections.emptyMap();
        }
    }

    private void addPendingMessages(RStream<String, String> stream,
                                    String group,
                                    int batchSize,
                                    Map<StreamMessageId, Map<String, String>> messages) {
        // 获取当前消费者的Pending消息
        List<PendingEntry> pendingEntries = stream.listPending(
                group, consumerName, StreamMessageId.MIN, StreamMessageId.MAX, batchSize
        );
        for (PendingEntry pendingEntry : pendingEntries) {
            if (messages.size() >= batchSize) break;
            StreamMessageId id = pendingEntry.getId();
            Map<StreamMessageId, Map<String, String>> messageMap = stream.read(StreamReadArgs.greaterThan(id));;
            if (!messageMap.isEmpty()) {
                messages.putAll(messageMap);
            }
        }
    }

    private void invokeMethod(HandlerMetaData handlerMetaData, List<String> messages) {
        Class<?> messageType = ((RedisMessageListener) handlerMetaData.getAnnotation()).messageType();
        if(messages.size() == 1) {
            if (messageType == String.class) {
                performInvoke(handlerMetaData, messages.getFirst());
                return;
            }
            performInvoke(handlerMetaData, JSONUtil.parse(messages.getFirst(), messageType));
            return;
        }
        StringBuilder jsonStr = new StringBuilder();
        for (String message : messages) {
            jsonStr.append(message).append(",");
        }
        String jsonList = '[' + jsonStr.substring(0, jsonStr.length() - 1) + ']';
        if (messageType == String.class) {
            performInvoke(handlerMetaData, jsonList);
            return;
        }
        performInvoke(handlerMetaData, JSONUtil.parseList(jsonList, messageType));
    }

    private void performInvoke(HandlerMetaData handlerMetaData, Object msgData) {
        try {
            handlerMetaData.getMethod().invoke(handlerMetaData.getTarget(), msgData);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String[] luaFilenames() {
        return new String[] {"satellite_range_consume.lua"};
    }
}
