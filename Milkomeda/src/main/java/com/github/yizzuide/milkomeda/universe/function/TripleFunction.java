package com.github.yizzuide.milkomeda.universe.function;

/**
 * TripleFunction
 *
 * @author yizzuide
 * Create at 2020/07/03 09:44
 */
@FunctionalInterface
public interface TripleFunction<T, U, V, R> {
    R apply(T t, U u, V v);
}
