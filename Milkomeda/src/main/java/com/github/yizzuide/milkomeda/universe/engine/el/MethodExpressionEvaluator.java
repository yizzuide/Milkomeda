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

import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.aop.support.AopUtils;
import org.springframework.context.expression.AnnotatedElementKey;
import org.springframework.context.expression.MethodBasedEvaluationContext;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.spel.support.StandardEvaluationContext;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * ExpressionEvaluator
 * el表达式解析器
 *
 * @author yizzuide
 * @since 1.5.0
 * @version 3.15.0
 * <br>
 * Create at 2019/05/30 22:24
 */
public class MethodExpressionEvaluator<T> extends AbstractExpressionEvaluator {

    // 目标方法缓存（提升查询性能）
    private final Map<AnnotatedElementKey, Method> targetMethodCache = new ConcurrentHashMap<>(64);

    /**
     * 根据方法创建一个 {@link EvaluationContext}
     * @param object        目标对象
     * @param targetClass   目标类型
     * @param method        方法
     * @param args          参数
     * @return  EvaluationContext
     */
    public StandardEvaluationContext createEvaluationContext(Object object, Class<?> targetClass,
                                                             Method method, Object[] args) {
        Method targetMethod = getTargetMethod(targetClass, method);
        // 封装方法根对象数据，表达式引用通过：root.* 和 args[x]
        MethodBasedRootObject rootObject = new MethodBasedRootObject(object, args);
        // 创建基于方法的执行上下文
        MethodBasedEvaluationContext evaluationContext = new MethodBasedEvaluationContext(rootObject, targetMethod, args, this.paramNameDiscoverer);
        // 注入其它变量
        configContext(evaluationContext, object);
        return evaluationContext;
    }

    /**
     * 根据指定的条件表达式获取值
     * @param expression        条件表达式
     * @param elementKey        元素key
     * @param evaluationContext 上下文
     * @param resultType        值类型
     * @return  解析出来的值
     */
    @SuppressWarnings("unchecked")
    public T condition(String expression, AnnotatedElementKey elementKey,
                       EvaluationContext evaluationContext, Class<T> resultType) {
        if (match(expression)) {
            return getExpression(this.expressionKeyCache, elementKey, expression)
                    .getValue(evaluationContext, resultType);
        }
        return (T) expression;
    }

    /**
     * 获取并缓存Method
     * @param targetClass   目标类
     * @param method        目标方法
     * @return  Method
     */
    private Method getTargetMethod(Class<?> targetClass, Method method) {
        AnnotatedElementKey methodKey = new AnnotatedElementKey(method, targetClass);
        Method targetMethod = this.targetMethodCache.get(methodKey);
        if (targetMethod == null) {
            targetMethod = AopUtils.getMostSpecificMethod(method, targetClass);
            this.targetMethodCache.put(methodKey, targetMethod);
        }
        return targetMethod;
    }

    /**
     * Root object for method which current invoked.
     * @since 3.15.0
     */
    @AllArgsConstructor
    @Data
    static class MethodBasedRootObject {
        /**
         * The root object is the method's target object.
         */
        private Object root;
        /**
         * The args is the method invoked.
         */
        private Object[] args;
    }
}  