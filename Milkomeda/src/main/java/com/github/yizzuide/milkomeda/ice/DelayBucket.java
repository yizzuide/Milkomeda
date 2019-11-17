package com.github.yizzuide.milkomeda.ice;

import java.util.List;

/**
 * DelayBucket
 *
 * @author yizzuide
 * @since 1.15.0
 * Create at 2019/11/16 16:03
 */
public interface DelayBucket {
    /**
     * 放入延时任务
     *
     * @param delayJob DelayJob
     */
    void add(DelayJob delayJob);

    /**
     * 批量放入延时任务
     * @param delayJobs List
     */
    void add(List<DelayJob> delayJobs);

    /**
     * 获得最新的延期任务
     *
     * @param index 指定的桶
     * @return DelayJob
     */
    DelayJob poll(Integer index);

    /**
     * 移除延时任务
     *
     * @param index     指定的桶
     * @param delayJob  DelayJob
     */
    void remove(Integer index, DelayJob delayJob);
}
