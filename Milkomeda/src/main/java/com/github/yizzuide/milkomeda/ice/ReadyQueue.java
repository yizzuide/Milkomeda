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

import org.springframework.data.redis.core.RedisOperations;

import java.util.List;

/**
 * ReadyQueue
 *
 * @author yizzuide
 * @since 1.15.0
 * @version 3.12.0
 * Create at 2019/11/16 17:04
 */
public interface ReadyQueue {
    /**
     * 添加待处理延迟任务
     * @param operations Pipelined操作
     * @param delayJob  DelayJob
     * @since 3.12.0
     */
    void push(RedisOperations<String, String> operations, DelayJob delayJob);

    /**
     * 取出待处理延迟任务
     * @param topic 任务分组
     * @return  DelayJob
     */
    DelayJob pop(String topic);

    /**
     * 批量取出待处理延迟任务（非原子操作，多线程可能会重复）
     * @param topic 任务分组
     * @param count 批量数
     * @return  List
     */
    List<DelayJob> pop(String topic, int count);

    /**
     * 获取准备消费队列的元素个数
     * @param topic 任务分组
     * @return ready queue size
     */
    long size(String topic);
}
