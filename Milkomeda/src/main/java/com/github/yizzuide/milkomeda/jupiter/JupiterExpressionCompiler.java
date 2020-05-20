package com.github.yizzuide.milkomeda.jupiter;

/**
 * JupiterExpressionCompiler
 * 表达式编译器
 *
 * @author yizzuide
 * @since 3.5.0
 * Create at 2020/05/19 21:41
 */
public interface JupiterExpressionCompiler {
    /**
     * 解析表达式
     * @param expression    表达式
     * @param root          根对象
     * @param resultType    返回类型
     * @param <T>           值类型
     * @return  解析结果
     */
    <T> T compile(String expression, Object root, Class<T> resultType);
}
