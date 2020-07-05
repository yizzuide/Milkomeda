package com.github.yizzuide.milkomeda.hydrogen.uniform;

/**
 * UniformAssert
 * 统一断言
 *
 * @author yizzuide
 * @since 3.10.0
 * Create at 2020/07/03 16:56
 */
public interface UniformExceptionAssert {

    /**
     * 创建异常
     * @param args  消息格式化参数
     * @return  UniformException
     */
    UniformException newException(Object... args);

    /**
     * 创建异常
     * @param t     被包装异常
     * @param args  消息格式化参数
     * @return  UniformException
     */
    UniformException newException(Throwable t, Object... args);

    /**
     * 断言是否为空
     * @param obj   被断言对象
     */
    default void assertNotNull(Object obj) {
        if (obj == null) {
            throw newException();
        }
    }

    /**
     * 断言是否为空
     * @param obj   被断言对象
     * @param args  消息格式化参数
     */
    default void assertNotNull(Object obj, Object... args) {
        if (obj == null) {
            throw newException(args);
        }
    }

    /**
     * 自定义条件断言
     * @param condition 断言条件
     * @param args  消息格式化参数
     */
    default void assertBool(boolean condition, Object... args) {
        if (condition) {
            throw newException(args);
        }
    }
}
