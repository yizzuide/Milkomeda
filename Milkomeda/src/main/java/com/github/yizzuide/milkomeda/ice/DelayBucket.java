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
 * DelayBucket
 *
 * @author yizzuide
 * @since 1.15.0
 * @version 3.14.0
 * <br>
 * Create at 2019/11/16 16:03
 */
public interface DelayBucket {

    /**
     * 放入延时任务
     * @param operations Pipelined操作
     * @param delayJob DelayJob
     * @return bucket index
     * @since 3.12.0
     */
    Integer add(RedisOperations<String, String> operations, DelayJob delayJob);

    /**
     * 批量放入延时任务
     * @param delayJobs List
     */
    void add(List<DelayJob> delayJobs);

    /**
     * 批量放入延时任务
     * @param operations Pipelined操作
     * @param delayJobs List
     * @since 3.12.0
     */
    void add(RedisOperations<String, String> operations, List<DelayJob> delayJobs);

    /**
     * 获得最新的延期任务
     *
     * @param index 指定的桶
     * @return DelayJob
     */
    DelayJob poll(Integer index);

    /**
     * 移除延时任务
     * @param index     指定的桶
     * @param delayJob  DelayJob
     */
    void remove(Integer index, DelayJob delayJob);
    /**
     * 移除延时任务
     * @param operations Pipelined操作
     * @param index     指定的桶
     * @param delayJob  DelayJob
     * @since 3.12.0
     */
    void remove(RedisOperations<String, String> operations, Integer index, DelayJob delayJob);
}
