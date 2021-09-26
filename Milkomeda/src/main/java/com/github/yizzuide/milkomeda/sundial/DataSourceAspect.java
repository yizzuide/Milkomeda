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

package com.github.yizzuide.milkomeda.sundial;

import com.github.yizzuide.milkomeda.util.ReflectUtil;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.core.annotation.Order;
import org.springframework.util.StringUtils;

/**
 * Sundial注解切面
 * @author jsq 786063250@qq.com
 * @since 3.4.0
 * @version 3.8.0
 * Create at 2020/5/8
 */
@Slf4j
@Order(999)
@Aspect
public class DataSourceAspect {

    @Pointcut("@within(com.github.yizzuide.milkomeda.sundial.Sundial) && execution(public * *(..))")
    public void classPointCut() {
    }


    @Pointcut("@annotation(com.github.yizzuide.milkomeda.sundial.Sundial) && execution(public * *(..))")
    public void actionPointCut() {
    }

    @Around("actionPointCut() || classPointCut()")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        Sundial sundial = ReflectUtil.getAnnotation(joinPoint, Sundial.class);
        if (StringUtils.isEmpty(sundial.value())) {
            return joinPoint.proceed();
        }
        SundialHolder.setDataSourceType(sundial.value());
        try {
            return joinPoint.proceed();
        } finally {
            SundialHolder.clearDataSourceType();
        }
    }
}
