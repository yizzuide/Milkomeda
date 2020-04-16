package com.github.yizzuide.milkomeda.ice;

import com.github.yizzuide.milkomeda.universe.context.ApplicationContextHolder;
import lombok.extern.slf4j.Slf4j;

/**
 * IceHolder
 * 一个管理Ice实例的类
 *
 * @author yizzuide
 * @since 3.0.0
 * @version 3.0.8
 * Create at 2020/04/09 14:03
 */
@Slf4j
public class IceHolder {

    private static Ice ice;

    private static DeadQueue deadQueue;

    private static DelayBucket delayBucket;

    static void setIce(Ice ice) {
        IceHolder.ice = ice;
    }

    static void setDeadQueue(DeadQueue deadQueue) {
        IceHolder.deadQueue = deadQueue;
    }

    static void setDelayBucket(DelayBucket delayBucket) {
        IceHolder.delayBucket = delayBucket;
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
        IceInstanceChangeEvent event = new IceInstanceChangeEvent(instanceName);
        ApplicationContextHolder.get().publishEvent(event);
    }

    /**
     * 重新激活所有TTR Overload的Job，并添加到DelayJobBucket
     */
    public static void activeDeadJobs() {
        DelayJob delayJob = deadQueue.pop();
        int count = 0;
        while (delayJob != null) {
            log.info("Ice 正在添加到延迟作业Job {}", delayJob);
            delayBucket.add(delayJob);
            delayJob = deadQueue.pop();
            count++;
        }
        if (count > 0) {
            log.info("Ice 添加到延迟总个数：{}", count);
        }
    }
}
