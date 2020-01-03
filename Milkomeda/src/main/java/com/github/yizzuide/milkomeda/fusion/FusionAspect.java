package com.github.yizzuide.milkomeda.fusion;

import com.github.yizzuide.milkomeda.universe.el.ELContext;
import lombok.Getter;
import lombok.Setter;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.core.annotation.Order;
import org.springframework.util.StringUtils;

import java.util.function.BiFunction;

import static com.github.yizzuide.milkomeda.util.ReflectUtil.getAnnotation;

/**
 * FusionAspect
 *
 * @author yizzuide
 * @since 1.12.0
 * @version 2.2.0
 * Create at 2019/08/09 11:09
 */
@Order(99)
@Aspect
public class FusionAspect {
    /**
     * 转换器
     */
    @Getter
    @Setter
    private BiFunction<String, Object, Object> converter;

    @Around("@annotation(fusion) || @within(fusion)")
    public Object doAround(ProceedingJoinPoint joinPoint, Fusion fusion) throws Throwable {
        // 在方法上的切面无法注入
        if (fusion == null) {
            fusion = getAnnotation(joinPoint, Fusion.class);
        }
        String allowed = fusion.allowed();
        // 如果条件不成功，不执行方法体
        if (!StringUtils.isEmpty(allowed)) {
            if (Boolean.parseBoolean(ELContext.getValue(joinPoint, allowed))) {
                return joinPoint.proceed();
            }
            return null;
        }

        String condition = fusion.condition();
        // 检查修改条件
        if (!StringUtils.isEmpty(condition) && !Boolean.parseBoolean(ELContext.getValue(joinPoint, condition))) {
            return joinPoint.proceed();
        }
        String tagName = fusion.value();
        // 执行方法体
        Object returnData = joinPoint.proceed();
        if (null ==  converter) {
            return returnData;
        }
        return converter.apply(tagName, returnData);
    }
}
