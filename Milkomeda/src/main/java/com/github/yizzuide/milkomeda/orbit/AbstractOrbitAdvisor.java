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
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.aop.support.AbstractGenericPointcutAdvisor;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.core.Ordered;
import org.springframework.util.CollectionUtils;

import java.util.Map;

/**
 * The base abstract class that orbit advisor subclasses need to extend.
 *
 * @see org.springframework.aop.framework.autoproxy.AbstractAdvisorAutoProxyCreator
 * @see org.springframework.aop.aspectj.annotation.AnnotationAwareAspectJAutoProxyCreator
 * @see org.springframework.beans.factory.config.RuntimeBeanNameReference
 * @since 3.15.0
 * @version 3.20.0
 * @author yizzuide
 * <br>
 * Create at 2022/02/26 00:58
 */
@NoArgsConstructor
@AllArgsConstructor
@Data
public abstract class AbstractOrbitAdvisor implements OrbitAdvisor {
    /**
     * The advisor id for register in spring context.
     */
    private String advisorId;

    /**
     * The advice bind with this advisor.
     */
    private Class<? extends OrbitAdvice> adviceClass;

    /**
     * Advice property values.
     */
    private Map<String, Object> adviceProps;

    /**
     * Advice order.
     */
    @Setter
    private Integer order = Ordered.LOWEST_PRECEDENCE;

    @Override
    public int getOrder() {
        return order;
    }

    @Override
    public void initFrom(OrbitProperties.Item orbitItem) {
        this.setAdvisorId(orbitItem.getKeyName());
        this.setAdviceClass(orbitItem.getAdviceClazz());
        this.setAdviceProps(orbitItem.getAdviceProps());
        if (orbitItem.getOrder() != null) {
            this.setOrder(orbitItem.getOrder());
        }
        if (!CollectionUtils.isEmpty(orbitItem.getAdvisorProps())) {
            ReflectUtil.setField(this, orbitItem.getAdvisorProps());
        }
    }

    @Override
    public BeanDefinition createAdvisorBeanDefinition(BeanDefinitionRegistry registry) {
        String adviceBeanName = OrbitAdviceRegisterHelper.register(this, registry);
        return this.createAdvisorBeanDefinitionBuilder()
                // 添加Bean引用，内部创建RuntimeBeanNameReference，延迟对Advice Bean的创建
                .addPropertyReference("advice", adviceBeanName)
                .addPropertyValue("order", getOrder())
                // 将Advisor作为基础设施Bean，不添加自动代理
                .setRole(BeanDefinition.ROLE_INFRASTRUCTURE)
                .getBeanDefinition();
    }

    /**
     * Extension hook that subclasses should create an advisor bean definition builder.
     * @return  the advisor must be extended of {@link AbstractGenericPointcutAdvisor}
     */
    protected abstract BeanDefinitionBuilder createAdvisorBeanDefinitionBuilder();
}
