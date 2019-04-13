package com.github.yizzuide.milkomeda.util;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.reflect.MethodSignature;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

/**
 * ReflectUtil
 * 反射工具类
 *
 * @author yizzuide
 * @since 0.2.0
 * Create at 2019/04/11 19:55
 */
public class ReflectUtil {
    /**
     * 获得方法上的注解
     * @param joinPoint 切点连接点
     * @param annotationClass 注解类型
     * @param <T> 注解类型
     * @return 注解实现
     * @throws Exception 反射异常
     */
    @SuppressWarnings("unchecked")
    public static <T extends Annotation> T getAnnotation(JoinPoint joinPoint, Class<T> annotationClass) throws Exception {
        // 目标类名
        String targetName = joinPoint.getTarget().getClass().getName();
        // 方法签名
        MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
        // 方法名
        String methodName = methodSignature.getName();
        // 参数类型
        Class[] parameterTypes = methodSignature.getParameterTypes();
        // 参数值
//        Object[] arguments = joinPoint.getArgs();
        // 目标字节类
        Class targetClass = Class.forName(targetName);
        // 反射方法
        Method method = targetClass.getDeclaredMethod(methodName, parameterTypes);
        // 获得方法上注解类与相关信息
        return method.getAnnotation(annotationClass);
    }
}
