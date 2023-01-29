/*
 * Copyright (c) 2023 yizzuide All rights Reserved.
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

import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.util.ClassUtils;

import java.beans.Introspector;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Simple helper class to register advice from advisor, it used {@link FactoryBean} for create bean.
 *
 * @since 3.15.0
 * @author yizzuide
 * <br>
 * Create at 2023/01/28 13:12
 */
public class OrbitAdviceRegisterHelper {
    /**
     * Bean name counter.
     */
    public static final Map<String, AtomicInteger> counterMap = new HashMap<>();

    /**
     * Register advice from OrbitAdvisor with {@link BeanDefinitionRegistry}.
     * @param orbitAdvisor  OrbitAdvisor
     * @param registry  BeanDefinitionRegistry
     * @return advice bean name
     */
    public static String register(OrbitAdvisor orbitAdvisor, BeanDefinitionRegistry registry) {
        // 根据BeanDefinition生成beanName
        //String adviceBeanName = AnnotationBeanNameGenerator.INSTANCE.generateBeanName(beanDefinition, (BeanDefinitionRegistry) beanFactory);
        // 根据类名生成beanName
        String shortClassName = ClassUtils.getShortName(orbitAdvisor.getAdviceClass());
        String adviceBeanName = Introspector.decapitalize(shortClassName);
        if (registry.containsBeanDefinition(adviceBeanName)) {
            BeanDefinition beanDefinition = registry.getBeanDefinition(adviceBeanName);
            Object adviceClass = beanDefinition.getPropertyValues().get("adviceClass");
            if (adviceClass == null || orbitAdvisor.getAdviceClass() == adviceClass) {
                return adviceBeanName;
            }
            return adviceBeanName + "$" + counterMap.get(adviceBeanName).getAndIncrement();
        } else {
            counterMap.put(adviceBeanName, new AtomicInteger(0));
        }
        AbstractBeanDefinition orbitAdviceFactoryBeanDefinition = BeanDefinitionBuilder.rootBeanDefinition(OrbitAdviceFactoryBean.class)
                .addPropertyValue("beanFactory", registry)
                .addPropertyValue("adviceClass", orbitAdvisor.getAdviceClass())
                .addPropertyValue("props", orbitAdvisor.getProps())
                .getBeanDefinition();
        registry.registerBeanDefinition(adviceBeanName, orbitAdviceFactoryBeanDefinition);
        return adviceBeanName;
    }
}
