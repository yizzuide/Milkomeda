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

package com.github.yizzuide.milkomeda.universe.engine.el;

import com.github.yizzuide.milkomeda.metal.MetalHolder;
import com.github.yizzuide.milkomeda.universe.context.ApplicationContextHolder;
import com.github.yizzuide.milkomeda.universe.context.WebContext;
import com.github.yizzuide.milkomeda.universe.extend.env.Environment;
import org.springframework.context.expression.CachedExpressionEvaluator;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.core.ParameterNameDiscoverer;
import org.springframework.expression.Expression;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Abstract cached expression evaluator.
 *
 * @since 3.15.0
 * @author yizzuide
 * <br>
 * Create at 2022/12/24 19:58
 */
public abstract class AbstractExpressionEvaluator extends CachedExpressionEvaluator {
    /**
     * 共享的参数名，基于内部缓存数据
     */
    protected final ParameterNameDiscoverer paramNameDiscoverer = new DefaultParameterNameDiscoverer();

    /**
     * 条件缓存
     */
    protected final Map<CachedExpressionEvaluator.ExpressionKey, Expression> expressionKeyCache = new ConcurrentHashMap<>(64);

    /**
     * Config evaluation context variables.
     * @param evaluationContext StandardEvaluationContext
     * @param root  root object
     */
    protected void configContext(StandardEvaluationContext evaluationContext, Object root) {
        // 目标对象：#target
        evaluationContext.setVariable("target", root);
        // 添加变量引用：#env[key]
        Environment env = ApplicationContextHolder.getEnvironment();
        if (env != null) {
            evaluationContext.setVariable("env", env.getProperties());
        }
        // 添加Metal配置：#metal[key]
        Map<String, String> metalSourceMap = MetalHolder.getSourceMap();
        if (metalSourceMap != null) {
            evaluationContext.setVariable("metal", metalSourceMap);
        }
        // 请求对象：#request, #reqParams[key]
        ServletRequestAttributes requestAttributes = WebContext.getRequestAttributes();
        if (requestAttributes != null) {
            evaluationContext.setVariable("request", requestAttributes.getRequest());
            evaluationContext.setVariable("reqParams", requestAttributes.getRequest().getParameterMap());
        }
    }
}
