package com.github.yizzuide.milkomeda.demo.orbit;

import com.github.yizzuide.milkomeda.orbit.OrbitAdvice;
import com.github.yizzuide.milkomeda.orbit.OrbitInvocation;
import lombok.extern.slf4j.Slf4j;

/**
 * RecordAdvice
 *
 * @author yizzuide
 * <br>
 * Create at 2023/01/28 01:34
 */
@Slf4j
public class RecordAdvice implements OrbitAdvice {
    @Override
    public Object invoke(OrbitInvocation invocation) throws Throwable {
        log.info("拦截支付参数：{}", invocation.getArgs());
        return invocation.proceed();
    }
}
