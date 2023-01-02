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

import com.github.yizzuide.milkomeda.universe.context.ApplicationContextHolder;
import org.springframework.context.ApplicationContext;
import org.springframework.context.expression.AnnotatedElementKey;
import org.springframework.context.expression.BeanFactoryResolver;
import org.springframework.expression.spel.support.StandardEvaluationContext;

/**
 * Object based expression evaluator.
 *
 * @since 3.15.0
 * @author yizzuide
 * <br>
 * Create at 2022/12/24 20:02
 */
public class ObjectExpressionEvaluator extends AbstractExpressionEvaluator {
    public <T> T condition(String expression, Object object, Class<T> resultType) {
        StandardEvaluationContext evaluationContext = new StandardEvaluationContext();
        ApplicationContext beanFactory = ApplicationContextHolder.tryGet();
        if (beanFactory != null) {
            BeanFactoryResolver beanFactoryResolver = new BeanFactoryResolver(beanFactory);
            evaluationContext.setBeanResolver(beanFactoryResolver);
        }
        // 创建自定义EL Root（EL获取： #this.object，#root.object）
        ExpressionRootObject root = new ExpressionRootObject(object, null);
        evaluationContext.setRootObject(root);
        configContext(evaluationContext, root);
        AnnotatedElementKey elementKey = new AnnotatedElementKey(root.getClass(), null);
        return getExpression(this.expressionKeyCache, elementKey, expression)
                .getValue(evaluationContext, resultType);
    }
}
