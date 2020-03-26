package com.github.yizzuide.milkomeda.hydrogen.core;

/**
 * HydrogenHolder
 *
 * @author yizzuide
 * @since 2.8.0
 * Create at 2020/03/26 20:25
 */
public class HydrogenHolder {
    private static HydrogenProperties props;

    public static void setProps(HydrogenProperties props) {
        HydrogenHolder.props = props;
    }

    public static HydrogenProperties getProps() {
        return props;
    }
}
