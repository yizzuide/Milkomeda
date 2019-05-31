package com.github.yizzuide.milkomeda.util;

import com.github.yizzuide.milkomeda.universe.el.ExpressionEvaluator;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.context.expression.AnnotatedElementKey;
import org.springframework.expression.EvaluationContext;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

/**
 * ReflectUtil
 * 反射工具类
 *
 * @author yizzuide
 * @since 0.2.0
 * @version 1.5.0
 * Create at 2019/04/11 19:55
 */
public class ReflectUtil {
    /**
     * 获得方法上的注解
     * @param joinPoint 切点连接点
     * @param annotationClass 注解类型
     * @param <T> 注解类型
     * @return 注解实现
     */
    public static <T extends Annotation> T getAnnotation(JoinPoint joinPoint, Class<T> annotationClass) {
        // 方法签名
        MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
        // 获得方法上注解类与相关信息
        return methodSignature.getMethod().getAnnotation(annotationClass);
    }

    /**
     * 根据EL表达式或内置头表达式抽取值
     * @param joinPoint 切面连接点
     * @param express   表达式
     * @return 解析的值
     */
    public static String extractValue(JoinPoint joinPoint, String express) {
        String value = express;
        // 解析EL表达式
        if (express.startsWith("#")) {
            value = ReflectUtil.getValue(joinPoint, express);
        } else if (express.startsWith("[") && express.endsWith("]") && express.length() > 2) { // 解析Http请求头
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            assert attributes != null;
            String headerName = express.substring(1, express.length() - 1);
            value = attributes.getRequest().getHeader(headerName);
            if (StringUtils.isEmpty(headerName)) {
                throw new IllegalArgumentException("Can't find " + headerName + " from HTTP header.");
            }
        }
        return value;
    }

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

    // EL表达式执行器
    private static ExpressionEvaluator<String> evaluator = new ExpressionEvaluator<>();

    /**
     * 根据类的反射信息，获取EL表达式的值
     * @param object        目标对象
     * @param args          参数
     * @param clazz         目标类型
     * @param method        方法
     * @param condition     el条件表达式
     * @return 解析的值
     */
    public static String getValue(Object object, Object[] args, Class clazz, Method method,
                          String condition) {
        if (args == null) {
            return null;
        }
        EvaluationContext evaluationContext =
                evaluator.createEvaluationContext(object, clazz, method, args);
        AnnotatedElementKey methodKey = new AnnotatedElementKey(method, clazz);
        return evaluator.condition(condition, methodKey, evaluationContext, String.class);
    }
}
