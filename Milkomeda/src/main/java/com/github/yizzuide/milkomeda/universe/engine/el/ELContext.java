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

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.context.ApplicationContext;
import org.springframework.context.expression.AnnotatedElementKey;
import org.springframework.context.expression.BeanFactoryResolver;
import org.springframework.expression.spel.support.StandardEvaluationContext;

import java.lang.reflect.Method;

/**
 * ELContext
 *
 * @author yizzuide
 * @since 2.0.0
 * @version 2.4.0
 * <br>
 * Create at 2019/12/20 12:05
 */
public class ELContext {
    // Bean工厂解析器
    private static BeanFactoryResolver beanFactoryResolver;

    /**
     * 根据方法切面，获取EL表达式的值
     * @param joinPoint 切面连接点
     * @param condition el条件表达式
     * @return 解析的值
     */
    public static String getValue(JoinPoint joinPoint, String condition) {
        return getValue(joinPoint.getTarget(), joinPoint.getArgs(),
                joinPoint.getTarget().getClass(),
                ((MethodSignature) joinPoint.getSignature()).getMethod(), condition, String.class);
    }

    /**
     * 根据方法切面，获取EL表达式的真实类型值
     * @param joinPoint         切面连接点
     * @param condition         el条件表达式
     * @param desiredResultType 返回类型
     * @param <T>   返回类型
     * @return  解析的值
     */
    public static <T> T getActualValue(JoinPoint joinPoint, String condition, Class<T> desiredResultType) {
        return getValue(joinPoint.getTarget(), joinPoint.getArgs(),
                joinPoint.getTarget().getClass(),
                ((MethodSignature) joinPoint.getSignature()).getMethod(), condition, desiredResultType);
    }

    /**
     * 根据类的反射信息，获取EL表达式的值
     * @param object            目标对象
     * @param args              参数
     * @param clazz             目标类型
     * @param method            方法
     * @param condition         el条件表达式
     * @param desiredResultType 返回类型
     * @param <T>   返回类型
     * @return  解析的值
     */
    public static <T> T getValue(Object object, Object[] args, Class<?> clazz, Method method,
                                  String condition, Class<T> desiredResultType) {
        if (args == null) {
            return null;
        }
        // EL表达式执行器
        ExpressionEvaluator<T> evaluator = new ExpressionEvaluator<>();
        // 创建AOP方法的执行上下文
        StandardEvaluationContext evaluationContext =
                evaluator.createEvaluationContext(object, clazz, method, args);
        // 设置Bean工厂解析器
        evaluationContext.setBeanResolver(beanFactoryResolver);
        AnnotatedElementKey methodKey = new AnnotatedElementKey(method, clazz);
        return evaluator.condition(condition, methodKey, evaluationContext, desiredResultType);
    }

    /**
     * 设置应用上下文
     * @param applicationContext    ApplicationContext
     */
    public static void setApplicationContext(ApplicationContext applicationContext) {
        beanFactoryResolver = new BeanFactoryResolver(applicationContext);
    }
}
