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
import com.github.yizzuide.milkomeda.universe.lang.Tuple;
import com.github.yizzuide.milkomeda.util.ReflectUtil;
import lombok.Getter;
import lombok.Setter;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.core.annotation.Order;
import org.springframework.util.StringUtils;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * FusionAspect
 *
 * @author yizzuide
 * @since 1.12.0
 * @version 3.13.0
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

    @Pointcut("@annotation(com.github.yizzuide.milkomeda.fusion.Fusion) && execution(public * *(..))")
    public void actionPointCut() {}
    // 多个@Fusion注解会识别为@Fusions
    @Pointcut("@annotation(com.github.yizzuide.milkomeda.fusion.Fusions) && execution(public * *(..))")
    public void actionPointSetCut() {}

    @Around("classPointCut() || actionPointCut() || actionPointSetCut()")
    public Object doAround(ProceedingJoinPoint joinPoint) throws Throwable {
        Method method = ((MethodSignature) joinPoint.getSignature()).getMethod();
        // 获取可重复注解
        Set<Fusion> fusions = AnnotatedElementUtils.getMergedRepeatableAnnotations(method, Fusion.class, Fusions.class);
        if (fusions.size() <= 1) {
            Fusion fusion = fusions.size() == 0 ? ReflectUtil.getAnnotation(joinPoint, Fusion.class)
                    : fusions.stream().findFirst().get();
            return fusionAroundApply(joinPoint, fusion);
        }
        return fusionGroupAroundApply(joinPoint, fusions);
    }

    private Object fusionGroupAroundApply(ProceedingJoinPoint joinPoint, Set<Fusion> fusionSet) throws Throwable {
        // 将allowed为空的排在后面
        List<Fusion> fusions = fusionSet.stream().sorted((f1, f2) -> {
            if (!StringUtils.hasLength(f1.allowed())) return 1;
            if (!StringUtils.hasLength(f2.allowed())) return -1;
            return 0;
        }).collect(Collectors.toList());

        // 返回值修改类型
        Fusion resultTagFusion = fusions.get(fusions.size() - 1);
        if (StringUtils.hasLength(resultTagFusion.allowed())) {
            resultTagFusion = null;
        }

        StringBuilder allowedExpress = new StringBuilder();
        String fallback = "";
        for (Fusion fusion : fusions) {
            // 忽略不是条件执行类型的
            if (fusion == resultTagFusion || !StringUtils.hasLength(fusion.allowed())) {
                continue;
            }
            // 拼接Spring EL执行语句
            // 忽略第一个的逻辑关系处理
            if (!StringUtils.hasLength(allowedExpress.toString())) {
                allowedExpress.append(fusion.allowed());
                fallback = fusion.fallback();
                continue;
            }
            allowedExpress.append(fusion.allowedType() == FusionAllowedType.AND ? " && " : " || ");
            allowedExpress.append(fusion.allowed());
            if (!StringUtils.hasLength(fusion.fallback())) {
                continue;
            }
            fallback = fusion.fallback();
        }
        Tuple<Boolean, Object> tuple = checkAllow(joinPoint, allowedExpress.toString(), fallback);
        boolean isAllowed = tuple.getT1();
        Object originReturnObj = tuple.getT2();
        // 不存在修改返回值功能，或者不允许执行
        if (resultTagFusion == null || !isAllowed) {
            return originReturnObj;
        }
        return modifyReturn(resultTagFusion, joinPoint, originReturnObj);
    }

    private Object fusionAroundApply(ProceedingJoinPoint joinPoint, Fusion fusion) throws Throwable {
        String allowed = fusion.allowed();
        if (StringUtils.hasLength(allowed)) {
            Tuple<Boolean, Object> tuple = checkAllow(joinPoint, allowed, fusion.fallback());
            boolean isAllowed = tuple.getT1();
            Object originReturnObj = tuple.getT2();
            // 允许执行，才能修改返回值
            if (isAllowed) {
                return modifyReturn(fusion, joinPoint, originReturnObj);
            }
        }
        return modifyReturn(fusion, joinPoint, null);
    }

    /**
     * 检测执行条件
     * @param joinPoint 切面连接点
     * @param allowed   放行EL表达式
     * @param fallback  反馈EL表达式
     * @return （是否允许执行，返回结果）
     * @throws Throwable 方法体异常
     */
    private Tuple<Boolean, Object> checkAllow(ProceedingJoinPoint joinPoint, String allowed, String fallback) throws Throwable {
        // 如果允许执行方法体
        if (Boolean.parseBoolean(ELContext.getValue(joinPoint, allowed))) {
            return Tuple.build(true, joinPoint.proceed());
        }
        // 没有设置反馈
        if (!StringUtils.hasLength(fallback)) {
            // 获取方法默认返回值
            return Tuple.build(false, ReflectUtil.getMethodDefaultReturnVal(joinPoint));
        }
        // 否则调用反馈方法
        return Tuple.build(false, ELContext.getActualValue(joinPoint, fallback, ReflectUtil.getMethodReturnType(joinPoint)));
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
        // 检查修改条件，条件不通过直接返回
        if (StringUtils.hasLength(condition) && !Boolean.parseBoolean(ELContext.getValue(joinPoint, condition))) {
            return originReturnObj == null ? joinPoint.proceed() : originReturnObj;
        }
        // 通过标签匹配修改返回值
        String tagName = !StringUtils.hasLength(resultTagFusion.value()) ? resultTagFusion.tag() : resultTagFusion.value();
        if (!StringUtils.hasLength(tagName)) {
            return originReturnObj;
        }
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
