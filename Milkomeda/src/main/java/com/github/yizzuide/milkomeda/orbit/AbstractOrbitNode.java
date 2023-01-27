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

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.aop.support.AbstractGenericPointcutAdvisor;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.util.ClassUtils;

import java.beans.Introspector;
import java.util.Map;

/**
 * The base abstract class of orbit node.
 *
 * @see org.springframework.aop.framework.autoproxy.AbstractAdvisorAutoProxyCreator
 * @see org.springframework.aop.aspectj.annotation.AnnotationAwareAspectJAutoProxyCreator
 * @since 3.15.0
 * @author yizzuide
 * <br>
 * Create at 2022/02/26 00:58
 */
@NoArgsConstructor
@AllArgsConstructor
@Data
public abstract class AbstractOrbitNode implements OrbitNode {
    /**
     * 通知器唯一标识
     */
    private String advisorId;

    /**
     * 通知类
     */
    private Class<? extends OrbitAdvice> adviceClass;

    /**
     * 通知属性列表
     */
    private Map<String, Object> props;

    @Override
    public BeanDefinition createAdvisorBeanDefinition(ConfigurableListableBeanFactory beanFactory) {
        BeanDefinitionRegistry registry = (BeanDefinitionRegistry) beanFactory;
        AbstractBeanDefinition orbitAdviceFactoryBeanDefinition = BeanDefinitionBuilder.rootBeanDefinition(OrbitAdviceFactoryBean.class)
                .addPropertyValue("beanFactory", beanFactory)
                .addPropertyValue("adviceClass", this.getAdviceClass())
                .addPropertyValue("props", this.getProps())
                .getBeanDefinition();
        // 根据BeanDefinition生成beanName
        //String adviceBeanName = AnnotationBeanNameGenerator.INSTANCE.generateBeanName(beanDefinition, (BeanDefinitionRegistry) beanFactory);
        String shortClassName = ClassUtils.getShortName(this.getAdviceClass());
        String adviceBeanName = Introspector.decapitalize(shortClassName);
        registry.registerBeanDefinition(adviceBeanName, orbitAdviceFactoryBeanDefinition);

        // 默认的自动代理会添加配置的Advisor Bean: AnnotationAwareAspectJAutoProxyCreator.findCandidateAdvisors() -> AbstractAdvisorAutoProxyCreator.getAdvicesAndAdvisorsForBean() -> findEligibleAdvisors() ->
        //  findCandidateAdvisors() -> BeanFactoryAdvisorRetrievalHelper.findAdvisor()
        return this.createAdvisorBeanDefinitionBuilder()
                // 使用Bean引用，内部创建RuntimeBeanReference，延迟对Advice Bean的创建（在其它自动配置都初始化完成后）
                .addPropertyReference("advice", adviceBeanName)
                .getBeanDefinition();
    }

    /**
     * Extension hook that subclasses should create an advisor bean definition builder.
     * @return  the advisor must be extended of {@link AbstractGenericPointcutAdvisor}
     * @since 3.15.0
     */
    protected abstract BeanDefinitionBuilder createAdvisorBeanDefinitionBuilder();
}
