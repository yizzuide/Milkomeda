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

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;

/**
 * Quark manager to create producer.
 *
 * @since 3.15.0
 * @author yizzuide
 * Create at 2023/08/20 10:23
 */
public class Quarks {

    static Integer bufferSize;

    private static Executor executor;

    private static List<QuarkEventHandler<?>> eventHandlerList;

    private static final Map<Long, QuarkProducer> producerMap = new ConcurrentHashMap<>();

    static void setBufferSize(Integer bufferSize) {
        Quarks.bufferSize = bufferSize;
    }

    static void setExecutor(Executor executor) {
        Quarks.executor = executor;
    }

    static void setEventHandlerList(List<QuarkEventHandler<?>> eventHandlerList) {
        Quarks.eventHandlerList = eventHandlerList;
    }

    /**
     * Bind a producer with identifier.
     * @param identifier such as user id.
     * @return  QuarkProducer
     */
    @SuppressWarnings({"deprecation", "unchecked"})
    public static QuarkProducer bindProducer(Long identifier) {
        if (producerMap.containsKey(identifier)) {
            return producerMap.get(identifier);
        }
        QuarkEventFactory<Object> eventFactory = new QuarkEventFactory<>();
        Disruptor<QuarkEvent<Object>> disruptor = new Disruptor<>(eventFactory, bufferSize, executor,
                ProducerType.SINGLE, new YieldingWaitStrategy());
        // handlers execute concurrently
        disruptor.handleEventsWith(eventHandlerList.toArray(new EventHandler[]{}));
        // this handler executes after upper handlers
        //eventHandlerGroup.then(h3);
        // h4 and h5 execute concurrently after h3
        //disruptor.after(h3).handleEventsWith(h4, h5);
        disruptor.start();
        RingBuffer<QuarkEvent<Object>> ringBuffer = disruptor.getRingBuffer();
        QuarkProducer quarkProducer = new QuarkProducer();
        quarkProducer.setRingBuffer(ringBuffer);
        quarkProducer.setDisruptor(disruptor);
        producerMap.put(identifier, quarkProducer);
        return quarkProducer;
    }

    @SuppressWarnings("unchecked")
    static QuarkProducer bindInnerProducer(Long identifier) {
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
        // WorkerPool contains a pool of WorkProcessors that will consume sequences
        WorkerPool<QuarkEvent<Object>> workerPool = new WorkerPool<>(ringBuffer, barrier,
                new FatalExceptionHandler(), eventHandlerList.toArray(new WorkHandler[]{}));
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
    public static void unbindProducer(Long identifier) {
        if (!producerMap.containsKey(identifier)) {
            return;
        }
        producerMap.get(identifier).getDisruptor().shutdown();
        producerMap.remove(identifier);
    }
}
