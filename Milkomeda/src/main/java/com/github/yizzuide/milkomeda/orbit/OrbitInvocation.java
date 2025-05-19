/*
 * Copyright (c) 2022 yizzuide All rights Reserved.
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

package com.github.yizzuide.milkomeda.orbit;

import com.github.yizzuide.milkomeda.universe.context.AopContextHolder;
import lombok.Builder;
import lombok.Getter;
import org.aspectj.lang.ProceedingJoinPoint;

import java.lang.reflect.Method;

/**
 * Method invoked meta info.
 *
 * @author yizzuide
 * @since 3.13.0
 * @version 3.21.0
 * <br>
 * Create at 2022/02/23 17:08
 */
@Getter
@Builder
public class OrbitInvocation {
    /**
     * 连接点
     */
    private ProceedingJoinPoint pjp;

    /**
     * 目标对象
     */
    private Object target;

    /**
     * 被代理目标类
     * @since 3.15.0
     */
    private Class<?> targetClass;

    /**
     * 切面方法
     */
    private Method method;

    /**
     * 调用参数
     */
    private Object[] args;

    /**
     * 获取被代理的目标对象（支持非当前代理目标类）
     * @param clazz Bean class
     * @return  Bean目标对象
     * @param <T>   Bean类型
     * @since 3.21.0
     */
    @SuppressWarnings("unchecked")
    public <T> T getTarget(Class<T> clazz) {
        if (clazz == targetClass) {
            return (T)target;
        }
        return AopContextHolder.getRealTarget(clazz);
    }

    /**
     * 调用被切面的方法执行
     * @return  原方法的返回结果
     */
    public Object proceed() {
        try {
            return pjp.proceed();
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 调用被切面的方法执行
     * @param args   参数
     * @return  原方法的返回结果
     * @since 3.21.0
     */
    public Object proceed(Object[] args) {
        try {
            return pjp.proceed(args);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }
}
