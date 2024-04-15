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

import com.github.yizzuide.milkomeda.comet.core.XCometContext;
import com.github.yizzuide.milkomeda.comet.core.XCometData;
import com.github.yizzuide.milkomeda.metal.MetalHolder;
import com.github.yizzuide.milkomeda.universe.context.ApplicationContextHolder;
import com.github.yizzuide.milkomeda.universe.context.WebContext;
import com.github.yizzuide.milkomeda.universe.extend.env.Environment;
import com.github.yizzuide.milkomeda.util.DateUtil;
import org.springframework.context.ApplicationContext;
import org.springframework.context.expression.AnnotatedElementKey;
import org.springframework.context.expression.BeanFactoryResolver;
import org.springframework.context.expression.CachedExpressionEvaluator;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.core.ParameterNameDiscoverer;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.Expression;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.util.CollectionUtils;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Abstract cached expression evaluator.
 *
 * @since 3.15.0
 * @version 3.20.0
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
     * Parse Spring EL with {@link EvaluateSource} and result type.
     * @param expression    Spring EL
     * @param source        context source
     * @param resultType    result type
     * @return  the parsed value
     * @param <T> result type
     * @since 3.20.0
     */
    public <T> T condition(String expression, EvaluateSource source, Class<T> resultType) {
        AnnotatedElementKey elementKey;
        // 基于目标对象的
        if (source.getMethod() == null) {
            elementKey = new AnnotatedElementKey(source.getTarget().getClass(), null);
        } else {
            elementKey = new AnnotatedElementKey(source.getMethod(), source.getTargetClass());
        }
        StandardEvaluationContext evaluationContext = createEvaluationContext(source);
        // 注入变量
        configContext(evaluationContext, source.getTarget());
        return getExpression(this.expressionKeyCache, elementKey, expression)
                .getValue(evaluationContext, resultType);
    }

    /**
     * Create instance of {@link EvaluationContext}.
     * @param source context source
     * @return  EvaluationContext
     * @since 3.20.0
     */
    protected abstract StandardEvaluationContext createEvaluationContext(EvaluateSource source);

    /**
     * Config evaluation context variables.
     * @param evaluationContext StandardEvaluationContext
     * @param root  root object
     */
    protected void configContext(StandardEvaluationContext evaluationContext, Object root) {
        // BeanFactoryResolver：@bean
        ApplicationContext beanFactory = ApplicationContextHolder.tryGet();
        if (beanFactory != null) {
            BeanFactoryResolver beanFactoryResolver = new BeanFactoryResolver(beanFactory);
            evaluationContext.setBeanResolver(beanFactoryResolver);
        }
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
        // XComet上下文：#ret、#fail、#xxx
        XCometData cometData = XCometContext.peek();
        if (cometData != null) {
            evaluationContext.setVariable("ret", cometData.getResult());
            if (cometData.getFailure() != null) {
                evaluationContext.setVariable("fail", cometData.getFailure().getMessage());
            }
            if (cometData.getVariables() != null) {
                Map<String, Object> variables = cometData.getVariables();
                if (!CollectionUtils.isEmpty(variables)) {
                    for (Map.Entry<String, Object> entry : variables.entrySet()) {
                        evaluationContext.setVariable(entry.getKey(), entry.getValue());
                    }
                }
            }
        }
        // 添加自定义函数
        try {
            Method add = DateUtil.class.getDeclaredMethod("getUnixTime");
            evaluationContext.registerFunction("now", add);
        } catch (Exception ignore) {}
    }
}
