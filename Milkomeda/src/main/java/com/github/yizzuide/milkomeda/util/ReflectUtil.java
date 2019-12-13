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
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

/**
 * ReflectUtil
 * 反射工具类
 *
 * @author yizzuide
 * @since 0.2.0
 * @version 2.0.0
 * Create at 2019/04/11 19:55
 */
public class ReflectUtil {

    /**
     * 获取父类或接口上泛型对应的Class
     * @param type  泛型原型
     * @return Class[]
     */
    public static Class<?>[] getClassOfParameterizedType(Type type) {
        // 是否带泛型
        if (type instanceof ParameterizedType) {
            ParameterizedType pt = (ParameterizedType)type;
            Type[] subTypes = pt.getActualTypeArguments();
            Class<?>[] classes = new Class[subTypes.length];
            for (int i = 0; i < subTypes.length; i++) {
                // 获取泛型类型
                Class<?> clazz = (Class<?>)((ParameterizedType) subTypes[i]).getRawType();
                classes[i] = clazz;
            }
            return classes;
        }
        return null;
    }

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
     * 注入参数
     * @param joinPoint 连接点
     * @param obj       注入值
     * @param type      注解
     * @param check     检查参数列表
     * @return  注入后的参数列表
     */
    public static Object[] injectParam(JoinPoint joinPoint, Object obj, Annotation type, boolean check) {
        Object[] args = joinPoint.getArgs();
        int len = args.length;
        for (int i = 0; i < len; i++) {
            if (obj.getClass().isInstance(args[i])) {
                args[i] = obj;
                return args;
            }
        }
        if (check) {
            throw new IllegalArgumentException("You must add " + obj.getClass().getSimpleName() + " parameter on method " +
                    joinPoint.getSignature().getName() + " before use @" + type.annotationType().getSimpleName() + ".");
        }
        return args;
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
        } else if (express.startsWith("@")) { // 解析Http请求头
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            assert attributes != null;
            String headerName = express.substring(1);
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
