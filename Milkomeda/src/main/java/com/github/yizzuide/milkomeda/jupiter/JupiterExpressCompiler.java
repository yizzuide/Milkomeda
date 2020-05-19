package com.github.yizzuide.milkomeda.jupiter;

/**
 * JupiterExpressCompiler
 * 表达式编译器
 *
 * @author yizzuide
 * @since 3.5.0
 * Create at 2020/05/19 21:41
 */
public interface JupiterExpressCompiler {
    /**
     * 解析表达式
     * @param express   表达式
     * @param context   上下文对象
     * @param <T>       值类型
     * @return  解析结果
     */
    <T> T compile(String express, Object context, Class<T> resultType);
}
