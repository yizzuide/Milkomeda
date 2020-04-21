package com.github.yizzuide.milkomeda.ice;

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
 * @version 3.1.0
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
