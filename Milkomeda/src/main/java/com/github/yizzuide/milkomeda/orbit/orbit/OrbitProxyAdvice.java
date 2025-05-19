/*
 * Copyright (c) 2025 yizzuide All rights Reserved.
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

package com.github.yizzuide.milkomeda.orbit.orbit;

import com.github.yizzuide.milkomeda.orbit.OrbitAdvice;
import com.github.yizzuide.milkomeda.orbit.OrbitInvocation;
import com.github.yizzuide.milkomeda.universe.aop.invoke.args.*;
import com.github.yizzuide.milkomeda.universe.metadata.HandlerMetaData;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.springframework.core.annotation.AnnotationUtils;

import java.util.List;

/**
 * An implement {@link OrbitAdvice} is pre-process for {@link OrbitProxy}. It connects from the method proxy
 * to the facet processing through Orbit's core proxy notification mechanism, and resolves the self-identification
 * parameters into around handle method from the proxy method.
 *
 * @since 3.21.0
 * @author yizzuide
 * Create at 2025/05/18 15:05
 */
public class OrbitProxyAdvice implements OrbitAdvice {
    @Override
    public Object invoke(OrbitInvocation invocation) throws Throwable {
        ProceedingJoinPoint joinPoint = invocation.getPjp();
        OrbitProxy orbitProxy = AnnotationUtils.findAnnotation(invocation.getMethod(), OrbitProxy.class);
        assert orbitProxy != null;
        String tag = orbitProxy.value();
        if (StringUtils.isEmpty(tag)) {
            tag = invocation.getMethod().getName();
        }
        List<HandlerMetaData> handlerMetaData = OrbitProxyConfig.getAroundMap().get(tag);
        // 一个方法代理只能有一个Around处理返回值
        HandlerMetaData metaData = handlerMetaData.get(0);
        if (metaData == null) {
            return invocation.proceed();
        }
        OrbitHandlerContext.setInvocation(invocation);
        ArgumentSources argumentSources = new ArgumentSources();
        String[] parameterNames = AbstractArgumentMatcher.NAME_DISCOVERER.getParameterNames(invocation.getMethod());
        if (parameterNames != null) {
            for (int i = 0; i < parameterNames.length; i++) {
                String paramName = parameterNames[i];
                argumentSources.add(new ArgumentDefinition(ArgumentMatchType.BY_NAME_PREFIX, paramName, Object.class, joinPoint.getArgs()[i]));
            }
        }
        argumentSources.add(new ArgumentDefinition(ArgumentMatchType.BY_TYPE,null, OrbitInvocation.class, invocation));
        Object[] args = MethodArgumentBinder.bind(argumentSources, metaData.getMethod());
        try {
            return metaData.getMethod().invoke(metaData.getTarget(), args);
        } finally {
            OrbitHandlerContext.clear();
        }
    }
}
