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
import org.aspectj.lang.JoinPoint;
import org.springframework.aop.support.AopUtils;
import org.springframework.context.expression.AnnotatedElementKey;
import org.springframework.context.expression.MethodBasedEvaluationContext;
import org.springframework.expression.spel.support.StandardEvaluationContext;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Method-based evaluator which uses with {@link JoinPoint}.
 *
 * @since 1.5.0
 * @version 3.20.0
 * @author yizzuide
 * <br>
 * Create at 2019/05/30 22:24
 */
public class MethodExpressionEvaluator<T> extends AbstractExpressionEvaluator {

    /**
     * 目标方法缓存（提升查询性能）
     */
    private final Map<AnnotatedElementKey, Method> targetMethodCache = new ConcurrentHashMap<>(64);

    public StandardEvaluationContext createEvaluationContext(EvaluateSource source) {
        Method targetMethod = getTargetMethod(source.getTargetClass(), source.getMethod());
        // 封装方法根对象数据，表达式引用通过：root.* 和 args[x]
        MethodBasedRootObject rootObject = new MethodBasedRootObject(source.getTarget(), source.getArgs());
        // 创建基于方法的执行上下文
        return new MethodBasedEvaluationContext(rootObject, targetMethod, source.getArgs(), this.paramNameDiscoverer);
    }

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