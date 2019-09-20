package com.github.yizzuide.milkomeda.fusion;

import com.github.yizzuide.milkomeda.util.ReflectUtil;
import lombok.Getter;
import lombok.Setter;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.util.StringUtils;

import java.util.function.BiFunction;

/**
 * FusionAspect
 *
 * @author yizzuide
 * @since 1.12.0
 * Create at 2019/08/09 11:09
 */
@Aspect
public class FusionAspect {
    /**
     * 记录器
     */
    @Getter
    @Setter
    private BiFunction<String, Object, Object> converter;

    // 切入点
    @Pointcut("@annotation(com.github.yizzuide.milkomeda.fusion.Fusion)")
    public void fusion() {}

    @Around("fusion()")
    public Object doAround(ProceedingJoinPoint joinPoint) throws Throwable {
        Fusion fusion = ReflectUtil.getAnnotation(joinPoint, Fusion.class);
        String tagName = StringUtils.isEmpty(fusion.value()) ? fusion.tag() : fusion.value();
        // 执行方法体
        Object returnData = joinPoint.proceed();
        if (null ==  converter) {
            return returnData;
        }
        return converter.apply(tagName, returnData);
    }
}
