package com.github.yizzuide.milkomeda.universe.function;

/**
 * Callback
 * 回调函数式接口
 *
 * @author yizzuide
 * @since 1.15.0
 * Create at 2019/11/18 15:26
 */
@FunctionalInterface
public interface Callback {
    /**
     * 回调方法
     * @throws Exception 回调异常
     */
    void call() throws Exception;
}
