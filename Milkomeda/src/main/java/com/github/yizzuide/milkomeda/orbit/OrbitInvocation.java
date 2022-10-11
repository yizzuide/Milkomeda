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

import lombok.Builder;
import lombok.Getter;
import org.aspectj.lang.ProceedingJoinPoint;

import java.lang.reflect.Method;

/**
 * OrbitInvocation
 * 调用对象
 *
 * @author yizzuide
 * @since 3.13.0
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
     * 切面方法
     */
    private Method method;
    /**
     * 调用参数
     */
    private Object[] args;

    /**
     * 调用被切面的方法执行
     * @return  原方法的返回结果
     * @throws Throwable    可抛出异常
     */
    public Object proceed() throws Throwable {
        return pjp.proceed();
    }
}
