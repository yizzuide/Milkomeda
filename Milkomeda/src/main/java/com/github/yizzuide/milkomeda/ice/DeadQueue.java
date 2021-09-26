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
 * DeadQueue
 *
 * @author yizzuide
 * @since 3.0.8
 * @version 3.12.0
 * Create at 2020/04/17 00:40
 */
public interface DeadQueue {

    /**
     * 放入Dead Queue
     * @param operations Pipelined操作
     * @param delayJob DelayJob
     * @since 3.12.0
     */
    void add(RedisOperations<String, String> operations, DelayJob delayJob);

    /**
     * 获取TTR Overload的DelayJob
     * @return  DelayJob
     */
    DelayJob pop();

    /**
     * 获取指定延迟Job个数
     * @param count 获取数量
     * @return  延迟Job列表
     */
    List<DelayJob> pop(long count);

    /**
     * 获得所有TTR Overload的DelayJob
     *
     * @return DelayJob数组
     */
    List<DelayJob> popALL();
}
