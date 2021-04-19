package com.github.yizzuide.milkomeda.sundial;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;

/**
 * DelegatingDataSourceAdvice
 * 数据源切面代理
 *
 * @author yizzuide
 * @since 3.4.0
 * Create at 2020/05/11 16:29
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class DelegatingDataSourceAdvice implements MethodInterceptor {

    private String keyName = DynamicRouteDataSource.MASTER_KEY;

    @Override
    public Object invoke(MethodInvocation methodInvocation) throws Throwable {
        // ProxyMethodInvocation pmi = (ProxyMethodInvocation) methodInvocation;
        // ProceedingJoinPoint pjp = new MethodInvocationProceedingJoinPoint(pmi);
        // Method method = methodInvocation.getMethod();
        try {
            SundialHolder.setDataSourceType(getKeyName());
            return methodInvocation.proceed();
        } finally {
            SundialHolder.clearDataSourceType();
        }
    }
}
