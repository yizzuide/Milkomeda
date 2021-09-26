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

import org.aopalliance.aop.Advice;
import org.springframework.aop.aspectj.AspectJExpressionPointcutAdvisor;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.env.Environment;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.lang.NonNull;
import org.springframework.util.CollectionUtils;

import java.util.List;

/**
 * DelegatingBeanDefinitionRegistrar
 * 动态注册数据选择策略切面
 *
 * <br><br>
 * ImportBeanDefinitionRegistrar是Bean定义阶段注册器，适用于动态注册一个AspectJExpressionPointcutAdvisor <br>
 * BeanPostProcessor是Bean的创建后置处理器，然后给它包装一个代理，但这里不适用
 *
 * @author yizzuide
 * @since 3.4.0
 * @see org.springframework.aop.aspectj.AspectJExpressionPointcutAdvisor
 * @see org.springframework.aop.support.AbstractExpressionPointcut
 * @see org.springframework.beans.factory.config.BeanPostProcessor
 * Create at 2020/05/11 17:31
 */
public class DelegatingBeanDefinitionRegistrar implements EnvironmentAware, ImportBeanDefinitionRegistrar {

    /**
     * 配置绑定
     */
    private Binder binder;

    @Override
    public void registerBeanDefinitions(@NonNull AnnotationMetadata importingClassMetadata,
                                        @NonNull BeanDefinitionRegistry registry) {
        SundialProperties props = binder.bind("milkomeda.sundial", SundialProperties.class).get();
        List<SundialProperties.Strategy> strategyList = props.getStrategy();
        if (CollectionUtils.isEmpty(strategyList)) {
            return;
        }
        for (SundialProperties.Strategy strategy : strategyList) {
            String beanName = "mk_sundial_advisor_" + strategy.getKeyName();
            Advice advice = new DelegatingDataSourceAdvice(strategy.getKeyName());
            BeanDefinition aspectJBean = BeanDefinitionBuilder.genericBeanDefinition(AspectJExpressionPointcutAdvisor.class)
                    .addPropertyValue("location", "$$aspectJAdvisor##") // Set the location for debugging.
                    .addPropertyValue("expression", strategy.getPointcutExpression())
                    .addPropertyValue("advice", advice)
                    .getBeanDefinition();
            registry.registerBeanDefinition(beanName, aspectJBean);
        }
    }

    @Override
    public void setEnvironment(@NonNull Environment environment) {
        binder = Binder.get(environment);
    }
}
