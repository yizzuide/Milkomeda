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

import com.github.yizzuide.milkomeda.util.ReflectUtil;
import lombok.extern.slf4j.Slf4j;
import org.aopalliance.aop.Advice;
import org.springframework.aop.aspectj.AspectJExpressionPointcutAdvisor;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.env.Environment;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.lang.NonNull;
import org.springframework.util.CollectionUtils;

import java.util.List;

/**
 * OrbitRegistrar
 * 环绕切面注册
 * <br><br>
 * ImportBeanDefinitionRegistrar是Bean定义阶段注册器，适用于动态注册一个AspectJExpressionPointcutAdvisor <br>
 * BeanPostProcessor是Bean的创建完成后置处理器，然后给它包装一个代理，但这里不适用
 *
 * @author yizzuide
 * @since 3.13.0
 * @see org.springframework.aop.aspectj.AspectJExpressionPointcutAdvisor
 * @see org.springframework.aop.support.AbstractExpressionPointcut
 * @see org.springframework.beans.factory.config.BeanPostProcessor
 * Create at 2022/02/21 01:14
 */
@Slf4j
public class OrbitRegistrar implements ImportBeanDefinitionRegistrar {

    OrbitRegistrar(Environment ignore) {}

    @Override
    public void registerBeanDefinitions(@NonNull AnnotationMetadata importingClassMetadata, @NonNull BeanDefinitionRegistry registry) {
        OrbitProperties orbitProperties = AbstractOrbitSource.getOrbitProperties();
        List<OrbitProperties.Item> instances = orbitProperties.getInstances();
        if (CollectionUtils.isEmpty(instances)) {
            return;
        }
        for (OrbitProperties.Item item : instances) {
            String beanName = "mk_sundial_advisor_" + item.getKeyName();
            try {
                Advice advice = item.getAdviceClassName().newInstance();
                // set custom props
                ReflectUtil.setField(advice, item.getProps());
                BeanDefinition aspectJBean = BeanDefinitionBuilder.genericBeanDefinition(AspectJExpressionPointcutAdvisor.class)
                        .addPropertyValue("location", String.format("$$%s##", item.getKeyName())) // Set the location for debugging.
                        .addPropertyValue("expression", item.getPointcutExpression())
                        .addPropertyValue("advice", advice)
                        .getBeanDefinition();
                registry.registerBeanDefinition(beanName, aspectJBean);
            } catch (Exception e) {
                log.error("Create instance error with msg: {}", e.getMessage(), e);
            }
        }
    }
}
