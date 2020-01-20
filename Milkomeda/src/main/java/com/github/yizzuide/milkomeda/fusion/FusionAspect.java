package com.github.yizzuide.milkomeda.fusion;

import com.github.yizzuide.milkomeda.universe.el.ELContext;
import com.github.yizzuide.milkomeda.util.ReflectUtil;
import lombok.Getter;
import lombok.Setter;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.core.annotation.Order;
import org.springframework.util.StringUtils;

import static com.github.yizzuide.milkomeda.util.ReflectUtil.getAnnotation;

/**
 * FusionAspect
 *
 * @author yizzuide
 * @since 1.12.0
 * @version 2.4.0
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
    private FusionConverter<String, Object, Object> converter;

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
            String fallback = fusion.fallback();
            // 没有设置反馈
            if (StringUtils.isEmpty(fallback)) {
                // 获取方法默认返回值
                return ReflectUtil.getMethodDefaultReturnVal(joinPoint);
            }
            // 否则调用反馈方法
            return ELContext.getActualValue(joinPoint, fallback, ReflectUtil.getMethodReturnType(joinPoint));
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

        // 处理String类型
        if (returnData instanceof String) {
            String str = (String) returnData;
            // 如果有错误前缀
            if (str.startsWith(FusionConverter.ERROR_PREFIX)) {
                String error = str.substring(FusionConverter.ERROR_PREFIX.length());
                return converter.apply(tagName, null, error);
            }
        }
        return converter.apply(tagName, returnData, null);
    }
}
