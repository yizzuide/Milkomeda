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

import com.github.yizzuide.milkomeda.util.RecognizeUtil;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;

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
    // KP：++操作符包含三条指令
    // 1.把变量count从内存加载到CPU的寄存器；2.在寄存器中执行+1操作；3.把结果写回内存(缓存)。
    // 注：CPU只能保证指令级别的原子操作，而不是高级语言的操作符。
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
        String adviceBeanName = RecognizeUtil.getBeanName(orbitAdvisor.getAdviceClass());
        if (registry.containsBeanDefinition(adviceBeanName)) {
            BeanDefinition beanDefinition = registry.getBeanDefinition(adviceBeanName);
            Object adviceClass = beanDefinition.getPropertyValues().get("adviceClass");
            if (adviceClass == null || orbitAdvisor.getAdviceClass() == adviceClass) {
                return adviceBeanName;
            }
            adviceBeanName = adviceBeanName + "$" + counterMap.get(adviceBeanName).getAndIncrement();
        } else {
            counterMap.putIfAbsent(adviceBeanName, new AtomicInteger(0));
        }
        AbstractBeanDefinition orbitAdviceFactoryBeanDefinition = BeanDefinitionBuilder.rootBeanDefinition(OrbitAdviceFactoryBean.class)
                .addPropertyValue("beanFactory", registry)
                .addPropertyValue("adviceClass", orbitAdvisor.getAdviceClass())
                .addPropertyValue("adviceProps", orbitAdvisor.getAdviceProps())
                .getBeanDefinition();
        registry.registerBeanDefinition(adviceBeanName, orbitAdviceFactoryBeanDefinition);
        return adviceBeanName;
    }
}
