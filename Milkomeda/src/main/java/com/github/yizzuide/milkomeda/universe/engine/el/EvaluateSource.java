/*
 * Copyright (c) 2024 yizzuide All rights Reserved.
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

package com.github.yizzuide.milkomeda.universe.engine.el;

import com.github.yizzuide.milkomeda.orbit.OrbitInvocation;
import lombok.Builder;
import lombok.Data;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.reflect.MethodSignature;

import java.lang.reflect.Method;

/**
 * Evaluate contextual data source.
 *
 * @since 3.20.0
 * @author yizzuide
 * Create at 2024/01/30 21:37
 */
@Builder
@Data
public class EvaluateSource {
    /**
     * The target object.
     */
    private Object target;

    /**
     * The target class.
     */
    private Class<?> targetClass;

    /**
     * The execute method of target.
     */
    private Method method;

    /**
     * The method args of target.
     */
    private Object[] args;

    /**
     * Create from JoinPoint.
     * @param joinPoint JoinPoint
     * @return  EvaluateSource
     */
    public static EvaluateSource from(JoinPoint joinPoint) {
        return EvaluateSource.builder()
                .target(joinPoint.getTarget())
                .targetClass(joinPoint.getTarget().getClass())
                .method(((MethodSignature) joinPoint.getSignature()).getMethod())
                .args(joinPoint.getArgs())
                .build();
    }

    /**
     * Create from OrbitInvocation.
     * @param invocation   OrbitInvocation
     * @param target  target object
     * @return  EvaluateSource
     */
    public static EvaluateSource from(OrbitInvocation invocation, Object target) {
        return EvaluateSource.builder()
                .target(target == null ? invocation.getTarget() : target)
                .targetClass(invocation.getTargetClass())
                .method(invocation.getMethod())
                .args(invocation.getArgs())
                .build();
    }

    /**
     * Create from the target object.
     * @param target  target object
     * @return EvaluateSource
     */
    public static EvaluateSource from(Object target) {
        return EvaluateSource.builder()
                .target(target)
                .targetClass(target.getClass())
                .build();
    }
}
