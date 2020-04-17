package com.github.yizzuide.milkomeda.ice;

import java.util.List;

/**
 * DeadQueue
 *
 * @author yizzuide
 * @since 3.0.8
 * Create at 2020/04/17 00:40
 */
public interface DeadQueue {

    /**
     * 放入Dead Queue
     *
     * @param delayJob DelayJob
     */
    void add(DelayJob delayJob);

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
