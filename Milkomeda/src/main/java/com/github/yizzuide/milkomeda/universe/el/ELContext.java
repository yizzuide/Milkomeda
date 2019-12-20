package com.github.yizzuide.milkomeda.universe.el;

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
 * Create at 2019/12/20 12:05
 */
public class ELContext {
    // EL表达式执行器
    private static final ExpressionEvaluator<String> evaluator = new ExpressionEvaluator<>();
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
                ((MethodSignature) joinPoint.getSignature()).getMethod(), condition);
    }

    /**
     * 根据类的反射信息，获取EL表达式的值
     * @param object        目标对象
     * @param args          参数
     * @param clazz         目标类型
     * @param method        方法
     * @param condition     el条件表达式
     * @return 解析的值
     */
    public static String getValue(Object object, Object[] args, Class<?> clazz, Method method,
                                  String condition) {
        if (args == null) {
            return null;
        }
        // 创建AOP方法的执行上下文
        StandardEvaluationContext evaluationContext =
                evaluator.createEvaluationContext(object, clazz, method, args);
        // 设置Bean工厂解析器
        evaluationContext.setBeanResolver(beanFactoryResolver);
        AnnotatedElementKey methodKey = new AnnotatedElementKey(method, clazz);
        return evaluator.condition(condition, methodKey, evaluationContext, String.class);
    }

    /**
     * 设置应用上下文
     * @param applicationContext    ApplicationContext
     */
    public static void setApplicationContext(ApplicationContext applicationContext) {
        beanFactoryResolver = new BeanFactoryResolver(applicationContext);
    }
}
