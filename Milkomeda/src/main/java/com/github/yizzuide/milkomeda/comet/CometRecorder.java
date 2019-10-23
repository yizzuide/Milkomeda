package com.github.yizzuide.milkomeda.comet;

import javax.servlet.http.HttpServletRequest;

/**
 * CometRecorder
 * 采集记录器策略接口
 *
 * @author yizzuide
 * @since 0.2.0
 * @version 1.13.3
 * Create at 2019/04/11 19:45
 */
public interface CometRecorder {
    /**
     * 方法进入时触发
     *  @param prototype 采集数据原型
     * @param tag        分类 tag，可根据 tag 来区分不同的 prototype
     * @param request    请求对象，应用@CometX的切面时为null
     * @param args       方法参数
     */
    default void onRequest(CometData prototype, String tag, HttpServletRequest request, Object[] args) {}

    /**
     * 方法返回时触发
     * @param cometData     日志实体
     * @param returnData    返回数据。如果返回null，默认会返回returnData本身
     * @return  自定义返回对象
     */
    default Object onReturn(CometData cometData, Object returnData) {
        return returnData;
    }

    /**
     * 方法抛出异常时触发
     * @param cometData 日志实体
     * @param e         异常
     */
    default void onThrowing(CometData cometData, Exception e) {}
}
