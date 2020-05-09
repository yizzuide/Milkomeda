package com.github.yizzuide.milkomeda.sundial;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.annotation.Order;

import java.lang.reflect.Method;

/**
 * @SundialDynamicDataSource 注解切面
 * @date 2020/5/8
 * @author jsq 786063250@qq.com
 * @since 3.4.0
 */
@Slf4j
@Order(999)
@Aspect
public class DataSourceAspect {

    @Pointcut("@within(com.github.yizzuide.milkomeda.sundial.SundialDynamicDataSource) && execution(public * *(..))")
    public void classPointCut() {
    }


    @Pointcut("@annotation(SundialDynamicDataSource) && execution(public * *(..))")
    public void actionPointCut() {
    }

    @Around("actionPointCut() || classPointCut()")
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
