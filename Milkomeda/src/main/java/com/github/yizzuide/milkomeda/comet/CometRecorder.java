package com.github.yizzuide.milkomeda.comet;

import javax.servlet.http.HttpServletRequest;

/**
 * CometRecorder
 * 采集记录器
 *
 * @author yizzuide
 * @since 0.2.0
 * @version 0.2.4
 * Create at 2019/04/11 19:45
 */
public interface CometRecorder {
    /**
     * 日志实体原型，子类型根据业务可以对这个类型进行扩展
     * @return 在0.2.4中返回 null，之后版本可能会删除
     * @deprecated 0.2.4 使用注解<code>Comet(prototype=CometData.class)</code>代替
     */
    @Deprecated
    default CometData prototype() { return null;}

    /**
     * 请求触发时
     * @param prototype 采集数据原型
     * @param tag       请求分类 tag，可根据 tag 来区分不同的 prototype；如果没有指定 tag 名又指定了指定的 prototype，
     * @param request   请求对象
     */
    default void onRequest(CometData prototype, String tag, HttpServletRequest request) {}

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
