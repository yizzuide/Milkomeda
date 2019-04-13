package com.github.yizzuide.milkomeda.comet;

/**
 * CometRecorder
 * 采集记录器
 *
 * @author yizzuide
 * @since 0.2.0
 * @version 0.2.1
 * Create at 2019/04/11 19:45
 */
public interface CometRecorder {
    /**
     * 日志实体原型，子类型根据业务可以对这个类型进行扩展
     * @return 默认为CometData
     */
    default CometData prototype() { return new CometData();}

    /**
     * 请求触发时
     * @param cometData 日志实体
     */
    default void onRequest(CometData cometData) {}

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
