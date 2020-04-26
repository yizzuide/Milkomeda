package com.github.yizzuide.milkomeda.comet.core;

import com.github.yizzuide.milkomeda.comet.collector.CometCollectorProperties;

/**
 * CometHolder
 *
 * @author yizzuide
 * @since 3.0.0
 * Create at 2020/03/28 12:42
 */
public class CometHolder {
    private static CometProperties props;

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
}
