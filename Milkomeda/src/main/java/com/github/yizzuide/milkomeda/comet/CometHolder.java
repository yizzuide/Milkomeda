package com.github.yizzuide.milkomeda.comet;

/**
 * CometHolder
 *
 * @author yizzuide
 * @since 2.8.0
 * Create at 2020/03/28 12:42
 */
public class CometHolder {
    private static CometProperties props;

    static void setProps(CometProperties props) {
        CometHolder.props = props;
    }

    public static CometProperties getProps() {
        return props;
    }
}
