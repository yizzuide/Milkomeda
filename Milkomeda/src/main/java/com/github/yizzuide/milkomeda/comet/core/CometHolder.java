package com.github.yizzuide.milkomeda.comet.core;

import com.github.yizzuide.milkomeda.comet.collector.CometCollectorProperties;
import com.github.yizzuide.milkomeda.comet.logger.CometLoggerProperties;

/**
 * CometHolder
 *
 * @author yizzuide
 * @since 3.0.0
 * @version 3.11.0
 * Create at 2020/03/28 12:42
 */
public class CometHolder {
    private static CometProperties props;

    private static CometLoggerProperties logProps;

    private static CometCollectorProperties collectorProps;

    static void setProps(CometProperties props) {
        CometHolder.props = props;
    }

    static CometProperties getProps() {
        return props;
    }

    public static void setCollectorProps(CometCollectorProperties collectorProps) {
        CometHolder.collectorProps = collectorProps;
    }

    static CometCollectorProperties getCollectorProps() {
        return collectorProps;
    }

    public static void setLogProps(CometLoggerProperties logProps) {
        CometHolder.logProps = logProps;
    }

    static CometLoggerProperties getLogProps() {
        return logProps;
    }

    /**
     * 是否需要包装请求
     * @return 请求数据是否要被缓存起来采集
     * @since 3.11.0
     */
    public static boolean shouldWrapRequest() {
        return getProps().isEnableReadRequestBody();
    }

    /**
     * 是否需要包装响应
     * @return  响应是否要被缓存起来采集
     * @since 3.11.0
     */
    public static boolean shouldWrapResponse() {
        return getProps().isEnableReadResponseBody();
    }

    /**
     * 响应可否可读
     * @return  响应数据是否可以被探测
     * @since 3.11.0
     */
    public static boolean isResponseReadable() {
        return  (getCollectorProps() != null && getCollectorProps().isEnableTag()) ||
                (getLogProps() != null && getLogProps().isEnableResponse());
    }
}
