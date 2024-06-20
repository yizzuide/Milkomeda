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

import com.lmax.disruptor.RingBuffer;
import com.lmax.disruptor.dsl.Disruptor;
import lombok.Data;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Quark producer for holder {@link RingBuffer} and publish event.
 *
 * @since 3.15.0
 * @version 3.20.0
 * @author yizzuide
 * Create at 2023/08/19 13:03
 */
@Data
public final class QuarkProducer {

    private List<Disruptor<QuarkEvent<Object>>> disruptorList;

    private volatile RingBuffer<QuarkEvent<Object>>[] ringBuffers;

    private final String topic;

    private final int warningSize;

    private final AtomicInteger idx = new AtomicInteger(0);

    @SuppressWarnings({"unchecked"})
    public QuarkProducer(Disruptor<QuarkEvent<Object>> disruptor, String topic) {
        this.disruptorList = new CopyOnWriteArrayList<>();
        this.disruptorList.add(disruptor);

        this.topic = topic;
        this.warningSize = (int) (disruptor.getBufferSize() * Quarks.getWarningPercent());
        RingBuffer<QuarkEvent<Object>>[] ringBuffers = new RingBuffer[1];
        ringBuffers[0] = disruptor.getRingBuffer();
        this.ringBuffers = ringBuffers;
    }

    public <T> void publishEventData(T data) {
        checkAndResize();
        RingBuffer<QuarkEvent<Object>> ringBuffer = getCurrentRingBuffer();
        // first, get and occupy the next sequence
        // 通过自旋获取下一个位置，需要均衡当前线程的任务与环形缓存的大小
        long sequence = ringBuffer.next();
        try {
            // second, fill event into ring
            QuarkEvent<Object> event = ringBuffer.get(sequence);
            event.setData(data);
        } finally {
            // last, publish the sequence slot
            // publish方法必须放在finally中以确保必须得到调用
            // 如果某个请求的sequence未被提交将会堵塞后续的发布操作或者其他的Producer
            // 在事件发布后，这个sequence会传递给消费者（EventHandler）
            ringBuffer.publish(sequence);
        }
    }

    public long getRemainingCapacity() {
        return getCurrentRingBuffer().remainingCapacity();
    }

    void shutdown() {
        this.disruptorList.forEach(Disruptor::shutdown);
    }

    private RingBuffer<QuarkEvent<Object>> getCurrentRingBuffer() {
        // 高性能取模，ringBuffers.length必须为2的幂次方
        return ringBuffers[idx.get() & ringBuffers.length - 1];
    }

    @SuppressWarnings({"unchecked"})
    private void checkAndResize() {
        if (getRemainingCapacity() > warningSize) {
            return;
        }
        // is all ringBuffers filled over?
        int count = ringBuffers.length;
        if (count > 1) {
            for (int i = 0; i < count; i++) {
                RingBuffer<QuarkEvent<Object>> ringBuffer = ringBuffers[i];
                if (ringBuffer.remainingCapacity() > warningSize) {
                    // reset uses the previous index
                    idx.getAndSet(i);
                    return;
                }
            }
        }

        int newSize = count << 1;
        RingBuffer<QuarkEvent<Object>>[] ringBuffers = new RingBuffer[newSize];
        System.arraycopy(this.ringBuffers, 0, ringBuffers, 0, count);
        for (int i = count; i < newSize; i++) {
            Disruptor<QuarkEvent<Object>> disruptor = Quarks.createDisruptor(topic);
            this.disruptorList.add(disruptor);
            ringBuffers[i] = disruptor.getRingBuffer();
        }
        // reset uses the last index
        idx.getAndSet(count);
        this.ringBuffers = ringBuffers;
    }
}
