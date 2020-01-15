package com.github.yizzuide.milkomeda.universe.function;

/**
 * ThrowableFunction
 *
 * @author yizzuide
 * Create at 2019/12/18 17:31
 */
@FunctionalInterface
public interface ThrowableFunction<T, R> {
    /**
     * 可抛出异常的函数
     * @param t 入参
     * @return  出参
     * @throws Throwable 可抛出异常
     */
    R apply(T t) throws Throwable;
}
