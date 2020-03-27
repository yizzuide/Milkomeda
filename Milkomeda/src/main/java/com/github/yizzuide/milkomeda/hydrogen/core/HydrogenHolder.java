package com.github.yizzuide.milkomeda.hydrogen.core;

import javax.validation.Validator;

/**
 * HydrogenHolder
 *
 * @author yizzuide
 * @since 2.8.0
 * Create at 2020/03/26 20:25
 */
public class HydrogenHolder {
    /**
     * 配置数据
     */
    private static HydrogenProperties props;

    private static Validator validator;

    public static void setProps(HydrogenProperties props) {
        HydrogenHolder.props = props;
    }

    public static HydrogenProperties getProps() {
        return props;
    }

    public static void setValidator(Validator validator) {
        HydrogenHolder.validator = validator;
    }

    public static Validator getValidator() {
        return validator;
    }
}
