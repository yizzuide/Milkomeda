package com.github.yizzuide.milkomeda.ice;

import java.util.List;

/**
 * ReadyQueue
 *
 * @author yizzuide
 * @since 1.15.0
 * @version 1.15.2
 * Create at 2019/11/16 17:04
 */
public interface ReadyQueue {
    /**
     * 添加待处理延迟任务
     * @param delayJob  DelayJob
     */
    void push(DelayJob delayJob);

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
     */
    long size(String topic);
}
