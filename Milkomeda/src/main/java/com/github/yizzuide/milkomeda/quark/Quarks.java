/*
 * Copyright (c) 2023 yizzuide All rights Reserved.
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

package com.github.yizzuide.milkomeda.quark;

import com.lmax.disruptor.*;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.dsl.ProducerType;
import org.springframework.util.CollectionUtils;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;

/**
 * Quark manager to create producer.
 *
 * @since 3.15.0
 * @version 4.0.0
 * @author yizzuide
 * Create at 2023/08/20 10:23
 */
public class Quarks {

    static Integer bufferSize;

    private static Executor executor;

    private static Map<String, List<QuarkEventHandler<?>>> topicEventHandlerMap;

    private static Map<String, ExceptionHandler<?>> topicExceptionHandlerMap;

    private static final Map<Serializable, QuarkProducer> producerMap = new ConcurrentHashMap<>();

    static void setBufferSize(Integer bufferSize) {
        Quarks.bufferSize = bufferSize;
    }

    static void setExecutor(Executor executor) {
        Quarks.executor = executor;
    }

    static void setEventHandlerList(Map<String, List<QuarkEventHandler<?>>> topicEventHandlerMap) {
        Quarks.topicEventHandlerMap = topicEventHandlerMap;
    }

    static void setExceptionHandlerList(Map<String, ExceptionHandler<?>> topicExceptionHandlerMap) {
        Quarks.topicExceptionHandlerMap = topicExceptionHandlerMap;
    }

    /**
     * Bind a producer with identifier.
     * @param identifier what data belongs to identifier.
     * @param topic topic name which used event handler.
     * @return  QuarkProducer
     */
    @SuppressWarnings({"deprecation", "unchecked", "rawtypes"})
    public static QuarkProducer bindProducer(Serializable identifier, String topic) {
        if (producerMap.containsKey(identifier)) {
            return producerMap.get(identifier);
        }
        QuarkEventFactory<Object> eventFactory = new QuarkEventFactory<>();
        Disruptor<QuarkEvent<Object>> disruptor = new Disruptor<>(eventFactory, bufferSize, executor,
                ProducerType.SINGLE, new YieldingWaitStrategy());
        List<QuarkEventHandler<?>> quarkEventHandlers = topicEventHandlerMap.get(topic);
        // handlers execute concurrently
        disruptor.handleEventsWith(quarkEventHandlers.toArray(new EventHandler[]{}));
        // this handler executes after upper handlers
        // .then(h3);
        // h4 and h5 execute concurrently after h3
        // .after(h3).handleEventsWith(h4, h5);
        if (!CollectionUtils.isEmpty(topicExceptionHandlerMap) && topicExceptionHandlerMap.get(topic) != null) {
            quarkEventHandlers.forEach(eventHandler -> disruptor.handleExceptionsFor((EventHandler) eventHandler)
                .with(topicExceptionHandlerMap.get(topic)));
        }
        disruptor.start();
        RingBuffer<QuarkEvent<Object>> ringBuffer = disruptor.getRingBuffer();
        QuarkProducer quarkProducer = new QuarkProducer();
        quarkProducer.setRingBuffer(ringBuffer);
        quarkProducer.setDisruptor(disruptor);
        producerMap.put(identifier, quarkProducer);
        return quarkProducer;
    }

    @SuppressWarnings({"unchecked"})
    static QuarkProducer bindInnerProducer(Serializable identifier, String topic) {
        if (producerMap.containsKey(identifier)) {
            return producerMap.get(identifier);
        }
        RingBuffer<QuarkEvent<Object>> ringBuffer = RingBuffer.create(
                ProducerType.MULTI,
                new QuarkEventFactory<>(),
                bufferSize,
                new YieldingWaitStrategy());
        // Coordination barrier for tracking the cursor for publishers and sequence of dependent EventProcessors for processing a data structure
        SequenceBarrier barrier = ringBuffer.newBarrier();
        List<QuarkEventHandler<?>> quarkEventHandlers = topicEventHandlerMap.get(topic);
        ExceptionHandler<Object> exceptionHandler = null;
        if (!CollectionUtils.isEmpty(topicExceptionHandlerMap)) {
            exceptionHandler = (ExceptionHandler<Object>) topicExceptionHandlerMap.get(topic);
        }
        // WorkerPool contains a pool of WorkProcessors that will consume sequences
        WorkerPool<QuarkEvent<Object>> workerPool = new WorkerPool<>(ringBuffer, barrier,
                exceptionHandler, quarkEventHandlers.toArray(new WorkHandler[]{}));
        // Sync event handler sequence to ringbuffer
        ringBuffer.addGatingSequences(workerPool.getWorkerSequences());
        workerPool.start(executor);
        QuarkProducer quarkProducer = new QuarkProducer();
        quarkProducer.setRingBuffer(ringBuffer);
        producerMap.put(identifier, quarkProducer);
        return quarkProducer;
    }

    /**
     * unbind a producer with identifier for release resource.
     * @param identifier such as user id.
     */
    public static void unbindProducer(Serializable identifier) {
        if (!producerMap.containsKey(identifier)) {
            return;
        }
        producerMap.get(identifier).getDisruptor().shutdown();
        producerMap.remove(identifier);
    }
}
