package com.github.yizzuide.milkomeda.universe.function;

/**
 * Runnable
 *
 * 可抛出异常简单的函数接口
 *
 * @since 1.10.0
 * @author yizzuide
 * Create at 2019/07/02 11:44
 */
@FunctionalInterface
public interface Runnable {

    void run() throws Exception;

}
