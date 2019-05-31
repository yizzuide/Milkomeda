package com.github.yizzuide.milkomeda.universe.el;

import org.springframework.aop.support.AopUtils;
import org.springframework.context.expression.AnnotatedElementKey;
import org.springframework.context.expression.CachedExpressionEvaluator;
import org.springframework.context.expression.MethodBasedEvaluationContext;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.core.ParameterNameDiscoverer;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.Expression;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * ExpressionEvaluator
 * el表达式解析器
 *
 * @author yizzuide
 * @since 1.5.0
 * Create at 2019/05/30 22:24
 */
public class ExpressionEvaluator<T> extends CachedExpressionEvaluator {

    // 共享的参数名，基于内部缓存数据
    private final ParameterNameDiscoverer paramNameDiscoverer =
            new DefaultParameterNameDiscoverer();

    // 条件缓存
    private final Map<ExpressionKey, Expression> conditionCache = new ConcurrentHashMap<>(64);

    // 目标方法缓存
    private final Map<AnnotatedElementKey, Method> targetMethodCache = new ConcurrentHashMap<>(64);

    /**
     * 根据方法创建一个 {@link EvaluationContext}
     * @param object        目标对象
     * @param targetClass   目标类型
     * @param method        方法
     * @param args          参数
     * @return  EvaluationContext
     */
    public EvaluationContext createEvaluationContext(Object object, Class<?> targetClass,
                                                     Method method, Object[] args) {

        Method targetMethod = getTargetMethod(targetClass, method);
        ExpressionRootObject root = new ExpressionRootObject(object, args);
        return new MethodBasedEvaluationContext(root, targetMethod, args, this.paramNameDiscoverer);
    }


    /**
     * 根据指定的条件表达式获取值
     * @param conditionExpression   条件表达式
     * @param elementKey            元素key
     * @param evalContext           上下文
     * @param clazz                 值类型
     * @return
     */
    public T condition(String conditionExpression, AnnotatedElementKey elementKey,
                       EvaluationContext evalContext, Class<T> clazz) {
        return getExpression(this.conditionCache, elementKey, conditionExpression)
                .getValue(evalContext, clazz);
    }

    private Method getTargetMethod(Class<?> targetClass, Method method) {
        AnnotatedElementKey methodKey = new AnnotatedElementKey(method, targetClass);
        Method targetMethod = this.targetMethodCache.get(methodKey);
        if (targetMethod == null) {
            targetMethod = AopUtils.getMostSpecificMethod(method, targetClass);
            if (targetMethod == null) {
                targetMethod = method;
            }
            this.targetMethodCache.put(methodKey, targetMethod);
        }
        return targetMethod;
    }
}  