package com.github.yizzuide.milkomeda.universe.el;

import org.springframework.aop.support.AopUtils;
import org.springframework.context.expression.AnnotatedElementKey;
import org.springframework.context.expression.CachedExpressionEvaluator;
import org.springframework.context.expression.MethodBasedEvaluationContext;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.core.ParameterNameDiscoverer;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.Expression;
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
 * Create at 2019/05/30 22:24
 */
public class ExpressionEvaluator<T> extends CachedExpressionEvaluator {

    // 共享的参数名，基于内部缓存数据
    private final ParameterNameDiscoverer paramNameDiscoverer =
            new DefaultParameterNameDiscoverer();

    // 条件缓存
    private final Map<ExpressionKey, Expression> conditionCache = new ConcurrentHashMap<>(64);

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
        // 创建自定义EL Root
        ExpressionRootObject root = new ExpressionRootObject(object, args);
        // 创建基于方法的执行上下文
        return new MethodBasedEvaluationContext(root, targetMethod, args, this.paramNameDiscoverer);
    }

    /**
     * 根据指定的条件表达式获取值
     * @param conditionExpression   条件表达式
     * @param elementKey            元素key
     * @param evalContext           上下文
     * @param clazz                 值类型
     * @return  解析出来的值
     */
    public T condition(String conditionExpression, AnnotatedElementKey elementKey,
                       EvaluationContext evalContext, Class<T> clazz) {
        return getExpression(this.conditionCache, elementKey, conditionExpression)
                .getValue(evalContext, clazz);
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
}  