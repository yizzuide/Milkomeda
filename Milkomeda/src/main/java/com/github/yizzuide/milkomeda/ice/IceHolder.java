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

import com.github.yizzuide.milkomeda.ice.inspector.JobInspector;
import com.github.yizzuide.milkomeda.universe.context.ApplicationContextHolder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.stream.Collectors;

/**
 * IceHolder
 * 一个管理Ice实例的类
 *
 * @author yizzuide
 * @since 3.0.0
 * @version 3.14.0
 * <br />
 * Create at 2020/04/09 14:03
 */
@Slf4j
public class IceHolder {

    private static IceProperties props;

    private static Ice ice;

    private static DeadQueue deadQueue;

    private static DelayBucket delayBucket;

    private static JobInspector jobInspector;

    private static String applicationName;

    static void setProps(IceProperties props) {
        IceHolder.props = props;
        setApplicationName(props.getInstanceName());
    }

    public static IceProperties getProps() {
        return props;
    }

    static void setIce(Ice ice) {
        IceHolder.ice = ice;
    }

    static void setDeadQueue(DeadQueue deadQueue) {
        IceHolder.deadQueue = deadQueue;
    }

    static void setDelayBucket(DelayBucket delayBucket) {
        IceHolder.delayBucket = delayBucket;
    }

    public static void setJobInspector(JobInspector jobInspector) {
        IceHolder.jobInspector = jobInspector;
    }

    public static JobInspector getJobInspector() {
        return jobInspector;
    }

    public static void setApplicationName(String applicationName) {
        IceHolder.applicationName = applicationName;
    }

    public static String getApplicationName() {
        return applicationName;
    }

    /**
     * 获取Ice实现
     * @return  Ice
     */
    public static Ice getIce() {
        return ice;
    }

    /**
     * 修改实例名（用于多产品）
     * @param instanceName  实例名
     * @since 3.0.7
     */
    public static void setInstanceName(String instanceName) {
        setApplicationName(instanceName);
        IceInstanceChangeEvent event = new IceInstanceChangeEvent(instanceName);
        ApplicationContextHolder.get().publishEvent(event);
    }

    /**
     * 重新激活所有Dead Queue的Job，并添加到延迟队列（用于启动时调用）
     * @since 3.0.8
     */
    public static void activeDeadJobs() {
        int retrieveCount = 10;
        List<DelayJob> delayJobs = deadQueue.pop(retrieveCount);
        if (CollectionUtils.isEmpty(delayJobs)) {
            return;
        }
        int count = 0;
        while (!CollectionUtils.isEmpty(delayJobs)) {
            log.info("Ice 正在Dead Queue恢复延迟作业Jobs {}", delayJobs.stream().map(DelayJob::getJodId).collect(Collectors.toList()));
            // 更新当前延迟时间
            delayJobs.forEach(DelayJob::updateDelayTime);
            delayBucket.add(delayJobs);
            count += delayJobs.size();
            // 如果恢复完成，退出
            if (delayJobs.size() != retrieveCount) {
                break;
            }
            delayJobs = deadQueue.pop(retrieveCount);
        }
        if (count > 0) {
            log.info("Ice 完成从Dead Queue恢复Job到延迟队列总个数：{}", count);
        }
    }
}
