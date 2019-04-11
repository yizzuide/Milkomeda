package com.github.yizzuide.milkomeda.comet;

/**
 * CometRecorder
 * 采集记录器
 *
 * @author yizzuide
 * Create at 2019/04/11 19:45
 */
public interface CometRecorder {
    /**
     * 方法返回结果后
     * @param cometData 日志实体
     */
    default void onReturn(CometData cometData) {}

    /**
     * 方法抛出异常
     * @param cometData 日志实体
     */
    default void onThrowing(CometData cometData) {}
}
