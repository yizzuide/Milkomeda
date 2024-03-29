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

/**
 * Quark producer for holder {@link RingBuffer} and publish event.
 *
 * @since 3.15.0
 * @author yizzuide
 * Create at 2023/08/19 13:03
 */
@Data
public class QuarkProducer {

    private Disruptor<QuarkEvent<Object>> disruptor;

    private RingBuffer<QuarkEvent<Object>> ringBuffer;

    public <T> void publishEventData(T data) {
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
}
