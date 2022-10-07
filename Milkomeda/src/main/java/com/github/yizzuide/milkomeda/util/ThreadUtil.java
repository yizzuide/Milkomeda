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

package com.github.yizzuide.milkomeda.util;

import com.github.yizzuide.milkomeda.pulsar.RequestContextDecorator;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.ThreadPoolExecutor;

/**
 * ThreadUtil
 *
 * @author yizzuide
 * @since 1.16.0
 * <br />
 * Create at 2019/11/23 01:09
 */
public class ThreadUtil {
    /**
     * 自定义配置ThreadPoolTaskExecutor
     *
     * @param taskExecutor     ThreadPoolTaskExecutor
     * @param prefix           前辍
     * @param corePoolSize     核心池大小
     * @param maxPoolSize      最大线程池数
     * @param queueCapacity    队列容量
     * @param keepAliveSeconds 线程保存存活时间
     */
    public static void configTaskExecutor(ThreadPoolTaskExecutor taskExecutor, String prefix, int corePoolSize, int maxPoolSize, int queueCapacity, int keepAliveSeconds) {
        // 线程池维护线程的最少数量
        taskExecutor.setCorePoolSize(corePoolSize);
        // 线程池维护线程的最大数量
        taskExecutor.setMaxPoolSize(maxPoolSize);
        // 线程池所使用的缓冲队列
        taskExecutor.setQueueCapacity(queueCapacity);
        // 线程池维护线程所允许的空闲时间
        taskExecutor.setKeepAliveSeconds(keepAliveSeconds);
        // 调度器shutdown被调用时等待当前被调度的任务完成
        taskExecutor.setWaitForTasksToCompleteOnShutdown(true);
        // 等待时长
        taskExecutor.setAwaitTerminationSeconds(60);
        taskExecutor.setThreadNamePrefix(prefix);
        // 线程池对拒绝任务（无线程可用）的处理策略，目前只支持AbortPolicy、CallerRunsPolicy，默认为后者
        taskExecutor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        taskExecutor.setTaskDecorator(new RequestContextDecorator());
        taskExecutor.initialize();
    }
}
