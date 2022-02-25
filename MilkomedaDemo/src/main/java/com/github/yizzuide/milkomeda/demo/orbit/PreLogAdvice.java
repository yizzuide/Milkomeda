package com.github.yizzuide.milkomeda.demo.orbit;

import com.github.yizzuide.milkomeda.orbit.OrbitAdvice;
import com.github.yizzuide.milkomeda.orbit.OrbitInvocation;
import lombok.extern.slf4j.Slf4j;

/**
 * PreLogMethodInterceptor
 *
 * @author yizzuide
 * Create at 2022/02/21 01:45
 */
@Slf4j
public class PreLogAdvice implements OrbitAdvice {
    @Override
    public Object invoke(OrbitInvocation invocation) throws Throwable {
        log.info("拦截请求参数：{}", invocation.getArgs());
        return invocation.proceed();
    }
}