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

package com.github.yizzuide.milkomeda.particle;

import com.github.yizzuide.milkomeda.universe.context.ApplicationContextHolder;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.core.annotation.Order;
import com.github.yizzuide.milkomeda.util.Strings;

import static com.github.yizzuide.milkomeda.util.ReflectUtil.*;

/**
 * ParticleAspect
 *
 * @author yizzuide
 * @since 1.5.0
 * @version 3.12.10
 * <br />
 * Create at 2019/05/30 22:29
 */
@Slf4j
@Aspect
@Order(88)
public class ParticleAspect {

    // 切入点
    @Pointcut("@annotation(com.github.yizzuide.milkomeda.particle.Limit)")
    public void particlePointCut() {}

    @Around("particlePointCut()")
    public Object doAround(ProceedingJoinPoint joinPoint) throws Throwable {
        val limit = getAnnotation(joinPoint, Limit.class);
        String beanName = limit.limiterBeanName();
        String prefix = limit.name();
        String key = limit.key();
        if (Strings.isEmpty(key)) {
            throw new IllegalArgumentException("You must set key for use Limit.");
        }
        // 解析表达式
        key = extractValue(joinPoint, key);
        Limiter limiter = !Strings.isEmpty(beanName) ? ApplicationContextHolder.get().getBean(beanName, Limiter.class)
                : ApplicationContextHolder.get().getBean(limit.limiterBeanClass());
        String decorateKey = Strings.isEmpty(prefix) ? key : prefix + ":" + key;
        return limiter.limit(decorateKey, (particle ->
                joinPoint.proceed(injectParam(joinPoint, particle, limit, true))));
    }
}
