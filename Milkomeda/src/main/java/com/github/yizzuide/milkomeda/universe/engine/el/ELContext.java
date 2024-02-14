/*
 * Copyright (c) 2021 yizzuide All rights Reserved.
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.github.yizzuide.milkomeda.universe.engine.el;

import com.github.yizzuide.milkomeda.orbit.OrbitInvocation;
import org.aspectj.lang.JoinPoint;

import java.util.Arrays;
import java.util.List;

/**
 * Method-based SpEL context.
 *
 * @author yizzuide
 * @since 2.0.0
 * @version 4.0.0
 * <br>
 * Create at 2019/12/20 12:05
 */
public class ELContext {

    /**
     * EL表达式匹配前缀
     */
    private static final List<String> EL_START_TOKENS = Arrays.asList("'", "@", "#", "T(", "root.", "args[", "true", "false");

    /**
     * 根据方法切面，获取EL表达式的值
     * @param joinPoint 切面连接点
     * @param condition el条件表达式
     * @return 解析的值
     */
    public static String getValue(JoinPoint joinPoint, String condition) {
        return getActualValue(joinPoint, condition, String.class);
    }

    /**
     * 根据方法切面，获取EL表达式的真实类型值
     * @param joinPoint     切面连接点
     * @param condition     el条件表达式
     * @param resultType    返回类型
     * @param <T>   返回类型
     * @return  解析的值
     */
    public static <T> T getActualValue(JoinPoint joinPoint, String condition, Class<T> resultType) {
        return getValue(EvaluateSource.from(joinPoint), condition, resultType);
    }

    /**
     * 获取EL表达式的值
     * @param object        目标对象
     * @param invocation    OrbitInvocation
     * @param condition     el条件表达式
     * @param resultType    返回类型
     * @param <T>   返回类型
     * @return  解析的值
     * @since 4.0.0
     */
    public static <T> T getValue(Object object, OrbitInvocation invocation, String condition, Class<T> resultType) {
        return getValue(EvaluateSource.from(invocation, object), condition, resultType);
    }

    /**
     * Get value from Spring EL.
     * @param source        context source
     * @param condition     Spring EL
     * @param resultType    return type
     * @param <T>   result type
     * @return  the parsed value
     * @since 4.0.0
     */
    @SuppressWarnings("unchecked")
    public static <T> T getValue(EvaluateSource source, String condition, Class<T> resultType) {
        if (match(condition)) {
            MethodExpressionEvaluator<T> evaluator = new MethodExpressionEvaluator<>();
            return evaluator.condition(condition, source, resultType);
        }
        return (T) condition;
    }

    /**
     * Match the Spring EL.
     * @param expression    Spring EL
     * @return true is matched
     * @since 4.0.0
     */
    public static boolean match(String expression) {
        return EL_START_TOKENS.stream().anyMatch(expression::startsWith);
    }
}
