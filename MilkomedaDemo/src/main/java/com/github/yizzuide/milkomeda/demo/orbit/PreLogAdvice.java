package com.github.yizzuide.milkomeda.demo.orbit;

import com.github.yizzuide.milkomeda.orbit.OrbitAdvice;
import com.github.yizzuide.milkomeda.orbit.OrbitInvocation;
import lombok.extern.slf4j.Slf4j;

/**
 * PreLogAdvice
 *
 * @author yizzuide
 * <br>
 * Create at 2022/02/21 01:45
 */
@Slf4j
// YAML配置方式（推荐，在功能和扩展性更强大）或@Orbit注解方式
//@Orbit(pointcutExpression = "execution(* com..orbit.*API.fetch*(..))")
public class PreLogAdvice implements OrbitAdvice {
    @Override
    public Object invoke(OrbitInvocation invocation) throws Throwable {
        log.info("拦截请求参数：{}", invocation.getArgs());
        return invocation.proceed();
    }
}
