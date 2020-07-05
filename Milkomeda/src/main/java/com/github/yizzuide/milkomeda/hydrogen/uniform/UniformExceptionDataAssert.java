package com.github.yizzuide.milkomeda.hydrogen.uniform;

import java.text.MessageFormat;

/**
 * UniformExceptionAssert
 * 统一异步断言，需要被枚举类实现
 *
 * @author yizzuide
 * @since 3.10.0
 * Create at 2020/07/03 17:10
 */
public interface UniformExceptionDataAssert extends UniformExceptionData, UniformExceptionAssert {

    @Override
    default UniformException newException(Object... args) {
        String msg = this.getMessage();
        if (args != null) {
            msg = MessageFormat.format(this.getMessage(), args);
        }
        return new UniformException(this, msg);
    }

    @Override
    default UniformException newException(Throwable t, Object... args) {
        String msg = this.getMessage();
        if (args != null) {
            msg = MessageFormat.format(this.getMessage(), args);
        }
        return new UniformException(this, msg, t);
    }
}
