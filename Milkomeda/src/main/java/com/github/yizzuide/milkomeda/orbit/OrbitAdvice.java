package com.github.yizzuide.milkomeda.orbit;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.aspectj.lang.ProceedingJoinPoint;
import org.springframework.aop.ProxyMethodInvocation;
import org.springframework.aop.aspectj.MethodInvocationProceedingJoinPoint;

import java.lang.reflect.Method;

/**
 * OrbitAdvice
 * 拦截切面
 *
 * @author yizzuide
 * @since 3.13.0
 * Create at 2022/02/23 16:53
 */
@FunctionalInterface
public interface OrbitAdvice extends MethodInterceptor {
    @Override
    default Object invoke(MethodInvocation invocation) throws Throwable {
         ProxyMethodInvocation pmi = (ProxyMethodInvocation) invocation;
         ProceedingJoinPoint pjp = new MethodInvocationProceedingJoinPoint(pmi);
         Method method = invocation.getMethod();
         OrbitInvocation orbitInvocation = OrbitInvocation.builder()
                .pjp(pjp)
                .target(pjp.getTarget())
                .method(method)
                .args(invocation.getArguments())
                .build();
        return invoke(orbitInvocation);
    }

    /**
     * 覆盖当前方法，执行前置或后置切面
     * @param invocation    调用数据
     * @return  使用`invocation.proceed()`返回结果
     * @throws Throwable    执行异常
     */
    Object invoke(OrbitInvocation invocation) throws Throwable;
}
