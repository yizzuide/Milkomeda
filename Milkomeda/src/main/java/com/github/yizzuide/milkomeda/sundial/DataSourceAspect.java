package com.github.yizzuide.milkomeda.sundial;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

/**
 * @date: 2020/5/7
 * @author: jsq
 * @email: 786063250@qq.com
 * @describe:
 */
@Order(999)
@Aspect
@Component
public class DataSourceAspect {

    @Pointcut("@annotation(com.github.yizzuide.milkomeda.sundial.SundialDynamicDataSource) || @within(com.github.yizzuide.milkomeda.sundial.SundialDynamicDataSource)")
    public void dsPointCut() {
    }

    @Around("dsPointCut()")
    public Object around(ProceedingJoinPoint point) throws Throwable {
        SundialDynamicDataSource sundialDynamicDataSource = getDataSource(point);
        if (null != sundialDynamicDataSource) {
            DynamicDataSourceContextHolder.setDataSourceType(sundialDynamicDataSource.value());
        }
        try {
            return point.proceed();
        } finally {
            // 销毁数据源 在执行方法之后
            DynamicDataSourceContextHolder.clearDataSourceType();
        }
    }

    /**
     * 获取需要切换的数据源
     */
    public SundialDynamicDataSource getDataSource(ProceedingJoinPoint point) {
        MethodSignature signature = (MethodSignature) point.getSignature();
        Class<? extends Object> targetClass = point.getTarget().getClass();
        SundialDynamicDataSource targetSundialDynamicDataSource = targetClass.getAnnotation(SundialDynamicDataSource.class);
        if (null != targetSundialDynamicDataSource) {
            return targetSundialDynamicDataSource;
        } else {
            Method method = signature.getMethod();
            return method.getAnnotation(SundialDynamicDataSource.class);
        }
    }
}
