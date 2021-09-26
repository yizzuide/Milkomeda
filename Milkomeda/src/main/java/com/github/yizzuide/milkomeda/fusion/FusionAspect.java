/*
 * Copyright (c) 2021 yizzuide All rights Reserved.
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.github.yizzuide.milkomeda.fusion;

import com.github.yizzuide.milkomeda.universe.el.ELContext;
import com.github.yizzuide.milkomeda.util.ReflectUtil;
import lombok.Getter;
import lombok.Setter;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.core.annotation.Order;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * FusionAspect
 *
 * @author yizzuide
 * @since 1.12.0
 * @version 3.1.3
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

    @Pointcut("@within(com.github.yizzuide.milkomeda.fusion.Fusion) && execution(public * *(..))")
    public void classPointCut() {}

    @Pointcut("@within(com.github.yizzuide.milkomeda.fusion.Fusion) && execution(public * *(..))")
    public void classGroupPointCut() {}

    @Pointcut("@annotation(com.github.yizzuide.milkomeda.fusion.Fusion) && execution(public * *(..))")
    public void actionPointCut() {}

    @Pointcut("@annotation(com.github.yizzuide.milkomeda.fusion.FusionGroup) && execution(public * *(..))")
    public void actionGroupPointCut() {}

    @Around("classPointCut() || actionPointCut() || classGroupPointCut() || actionGroupPointCut()")
    public Object doAround(ProceedingJoinPoint joinPoint) throws Throwable {
        Fusion fusion = ReflectUtil.getAnnotation(joinPoint, Fusion.class);
        if (fusion != null) {
            return fusionAroundApply(joinPoint, fusion);
        }
        FusionGroup fusionGroup = ReflectUtil.getAnnotation(joinPoint, FusionGroup.class);
        return fusionGroupAroundApply(joinPoint, fusionGroup);
    }

    private Object fusionGroupAroundApply(ProceedingJoinPoint joinPoint, FusionGroup fusionGroup) throws Throwable {
        List<Fusion> fusions = Stream.of(fusionGroup.value()).sorted((f1, f2) -> {
            if (StringUtils.isEmpty(f1.allowed())) return 1;
            if (StringUtils.isEmpty(f2.allowed())) return -1;
            return 0;
        }).collect(Collectors.toList());

        // 返回值修改类型
        Fusion resultTagFusion = fusions.get(fusions.size() - 1);
        if (!StringUtils.isEmpty(resultTagFusion.allowed())) {
            resultTagFusion = null;
        }

        StringBuilder allowedExpress = new StringBuilder();
        String fallback = "";
        for (Fusion fusion : fusions) {
            // 忽略不是条件执行类型的
            if (fusion == resultTagFusion || StringUtils.isEmpty(fusion.allowed())) {
                continue;
            }
            // 拼接Spring EL执行语句
            if (StringUtils.isEmpty(allowedExpress.toString())) {
                allowedExpress.append(fusion.allowed());
                fallback = fusion.fallback();
                continue;
            }
            if (fusion.allowedType() == FusionAllowedType.AND) {
                allowedExpress.append(" && ");
            } else {
                allowedExpress.append(" || ");
            }
            allowedExpress.append(fusion.allowed());
            if (StringUtils.isEmpty(fusion.fallback())) {
                continue;
            }
            fallback = fusion.fallback();
        }
        Object originReturnObj = checkAllow(joinPoint, allowedExpress.toString(), fallback);
        if (resultTagFusion == null) {
            return originReturnObj;
        }
        return modifyReturn(resultTagFusion, joinPoint, originReturnObj);
    }

    private Object fusionAroundApply(ProceedingJoinPoint joinPoint, Fusion fusion) throws Throwable {
        String allowed = fusion.allowed();
        if (!StringUtils.isEmpty(allowed)) {
            return checkAllow(joinPoint, allowed, fusion.fallback());
        }
        return modifyReturn(fusion, joinPoint, null);
    }

    /**
     * 检测执行条件
     * @param joinPoint 切面连接点
     * @param allowed   放行EL表达式
     * @param fallback  反馈EL表达式
     * @return  方法体返回值
     * @throws Throwable 方法体异常
     */
    private Object checkAllow(ProceedingJoinPoint joinPoint, String allowed, String fallback) throws Throwable {
        // 如果允许执行方法体
        if (Boolean.parseBoolean(ELContext.getValue(joinPoint, allowed))) {
            return joinPoint.proceed();
        }
        // 没有设置反馈
        if (StringUtils.isEmpty(fallback)) {
            // 获取方法默认返回值
            return ReflectUtil.getMethodDefaultReturnVal(joinPoint);
        }
        // 否则调用反馈方法
        return ELContext.getActualValue(joinPoint, fallback, ReflectUtil.getMethodReturnType(joinPoint));
    }

    /**
     * 修改返回值
     * @param resultTagFusion   Fusion
     * @param joinPoint         切面连接点
     * @param originReturnObj   原返回值
     * @return  实际返回值
     * @throws Throwable    方法体异常
     */
    private Object modifyReturn(Fusion resultTagFusion, ProceedingJoinPoint joinPoint, Object originReturnObj) throws Throwable {
        String condition = resultTagFusion.condition();
        // 检查修改条件
        if (!StringUtils.isEmpty(condition) && !Boolean.parseBoolean(ELContext.getValue(joinPoint, condition))) {
            return originReturnObj == null ? joinPoint.proceed() : originReturnObj;
        }
        String tagName = StringUtils.isEmpty(resultTagFusion.value()) ? resultTagFusion.tag() : resultTagFusion.value();
        // 执行方法体
        Object returnData = originReturnObj == null ? joinPoint.proceed() : originReturnObj;
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
