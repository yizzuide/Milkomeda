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

package com.github.yizzuide.milkomeda.universe.extend.processor;

import com.github.yizzuide.milkomeda.universe.config.MilkomedaProperties;
import com.github.yizzuide.milkomeda.universe.context.ApplicationContextHolder;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.boot.context.properties.bind.BindResult;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.context.annotation.AnnotationBeanNameGenerator;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.lang.NonNull;
import org.springframework.util.CollectionUtils;

import java.util.List;

/**
 * The processor used for registry bean definition early.
 *
 * @see org.springframework.context.annotation.ConfigurationClassPostProcessor
 * @see org.springframework.beans.factory.config.BeanFactoryPostProcessor
 * @see org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor
 * @since 3.15.0
 * @version 3.20.0
 * @author yizzuide
 * <br>
 * Create at 2023/04/27 20:44
 */
// BeanDefinitionRegistryPostProcessor是BeanFactoryPostProcessor子接口，而postProcessBeanDefinitionRegistry方法比postProcessBeanFactory调用更早，
//  处理BeanFactoryPostProcessor流程：AbstractApplicationContext.refresh() ->
//      invokeBeanFactoryPostProcessors() -> PostProcessorRegistrationDelegate.invokeBeanFactoryPostProcessors()
public class EarlyLoadBeanDefinitionRegistryPostProcessor implements BeanDefinitionRegistryPostProcessor {

    @Override
    public void postProcessBeanDefinitionRegistry(@NonNull BeanDefinitionRegistry registry) throws BeansException {
        // 将ApplicationContextHolder排在四个Spring内部Processor Bean后面注册
        BeanDefinition beanDefinition = new RootBeanDefinition(ApplicationContextHolder.class);
        String beanName = AnnotationBeanNameGenerator.INSTANCE.generateBeanName(beanDefinition, registry);
        registry.registerBeanDefinition(beanName, beanDefinition);

        // 加载其它配置的早期注册的Bean
        ConfigurableEnvironment configurableEnvironment = ApplicationContextHolder.getPendingConfigurableEnvironment();
        if(configurableEnvironment != null) {
            BindResult<MilkomedaProperties> bindResult = Binder.get(configurableEnvironment).bind(MilkomedaProperties.PREFIX, MilkomedaProperties.class);
            if (!bindResult.isBound()) {
                return;
            }
            MilkomedaProperties milkomedaProperties = bindResult.get();
            List<Class<?>> earlyRegisterBeans = milkomedaProperties.getRegisterEarlyBeans();
            if (CollectionUtils.isEmpty(earlyRegisterBeans)) {
                return;
            }
            earlyRegisterBeans.forEach(clazz -> {
                BeanDefinition earlyBeanDefinition = new RootBeanDefinition(clazz);
                String earlyBeanName = AnnotationBeanNameGenerator.INSTANCE.generateBeanName(earlyBeanDefinition, registry);
                registry.registerBeanDefinition(earlyBeanName, earlyBeanDefinition);
            });
        }
    }

    @Override
    public void postProcessBeanFactory(@NonNull ConfigurableListableBeanFactory beanFactory) throws BeansException {}
}
