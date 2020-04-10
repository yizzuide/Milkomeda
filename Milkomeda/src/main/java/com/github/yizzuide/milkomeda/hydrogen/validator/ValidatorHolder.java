package com.github.yizzuide.milkomeda.hydrogen.validator;

/**
 * ValidatorHolder
 *
 * @author yizzuide
 * @since 3.0.0
 * Create at 2020/04/06 01:03
 */
class ValidatorHolder {
    private static ValidatorProperties props;

    static void setProps(ValidatorProperties props) {
        ValidatorHolder.props = props;
    }

    static ValidatorProperties getProps() {
        return props;
    }
}
