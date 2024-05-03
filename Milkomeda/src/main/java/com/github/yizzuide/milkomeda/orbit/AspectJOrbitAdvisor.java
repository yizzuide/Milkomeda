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

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.springframework.aop.aspectj.AspectJExpressionPointcutAdvisor;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.core.Ordered;

import java.util.Map;

/**
 * AspectJ impl of orbit advisor metadata, which support set pointcut expression.
 *
 * @see org.springframework.aop.aspectj.AspectJExpressionPointcut
 * @since 3.15.0
 * @author yizzuide
 * <br>
 * Create at 2023/01/27 16:12
 */
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Data
public class AspectJOrbitAdvisor extends AbstractOrbitAdvisor {

    /**
     * The pointcut expression value is an AspectJ expression.
     */
    private String pointcutExpression;

    public AspectJOrbitAdvisor(String pointcutExpression, String id, Class<? extends OrbitAdvice> adviceClass, Map<String, Object> props) {
        super(id, adviceClass, props, Ordered.LOWEST_PRECEDENCE);
        this.pointcutExpression = pointcutExpression;
    }

    @Override
    public void initFrom(OrbitProperties.Item orbitItem) {
        if (orbitItem.getPointcutExpression() != null) {
            this.setPointcutExpression(orbitItem.getPointcutExpression());
        }
        super.initFrom(orbitItem);
    }

    @Override
    public BeanDefinitionBuilder createAdvisorBeanDefinitionBuilder() {
        return BeanDefinitionBuilder.rootBeanDefinition(AspectJExpressionPointcutAdvisor.class)
                .addPropertyValue("location", String.format("$$%s##", this.getAdvisorId())) // Set the location for debugging.
                .addPropertyValue("expression", this.getPointcutExpression());
    }
}
