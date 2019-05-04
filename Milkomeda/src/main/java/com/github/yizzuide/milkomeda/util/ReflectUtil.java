package com.github.yizzuide.milkomeda.util;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.reflect.MethodSignature;

import java.lang.annotation.Annotation;

/**
 * ReflectUtil
 * 反射工具类
 *
 * @author yizzuide
 * @since 0.2.0
 * @version 1.1.0
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
}
