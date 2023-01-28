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
import org.springframework.aop.support.DefaultPointcutAdvisor;
import org.springframework.aop.support.annotation.AnnotationMatchingPointcut;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;

import java.lang.annotation.Annotation;
import java.util.Map;

/**
 * Simple orbit node that Looks for a specific Java 5 annotation being present on a class or method.
 *
 * @see org.springframework.aop.support.AbstractGenericPointcutAdvisor
 * @see org.springframework.aop.support.annotation.AnnotationMatchingPointcut
 * @since 3.15.0
 * @author yizzuide
 * <br>
 * Create at 2023/01/27 16:14
 */
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Data
public class AnnotationOrbitAdvisor extends AbstractOrbitAdvisor {

    /**
     * The annotation type to look for at the class level.
     */
    private Class<? extends Annotation> classAnnotationType;

    /**
     * The annotation type to look for at the method level.
     */
    private Class<? extends Annotation> methodAnnotationType;

    public AnnotationOrbitAdvisor(Class<? extends Annotation> classAnnotationType, Class<? extends Annotation> methodAnnotationType,
                                  String advisorId, Class<? extends OrbitAdvice> adviceClass, Map<String, Object> props) {
        super(advisorId, adviceClass, props);
        this.classAnnotationType = classAnnotationType;
        this.methodAnnotationType = methodAnnotationType;
    }

    public static AnnotationOrbitAdvisor forClass(Class<? extends Annotation> classAnnotationType, String advisorId, Class<? extends OrbitAdvice> adviceClass, Map<String, Object> props) {
        return new AnnotationOrbitAdvisor(classAnnotationType, null, advisorId, adviceClass, props);
    }

    public static AnnotationOrbitAdvisor forMethod(Class<? extends Annotation> methodAnnotationType, String advisorId, Class<? extends OrbitAdvice> adviceClass, Map<String, Object> props) {
        return new AnnotationOrbitAdvisor(null, methodAnnotationType, advisorId, adviceClass, props);
    }

    @Override
    public BeanDefinitionBuilder createAdvisorBeanDefinitionBuilder() {
        return BeanDefinitionBuilder.rootBeanDefinition(DefaultPointcutAdvisor.class)
                .addPropertyValue("pointcut", new AnnotationMatchingPointcut(this.getClassAnnotationType(), this.getMethodAnnotationType()));
    }
}
