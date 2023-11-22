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

import com.github.yizzuide.milkomeda.util.ReflectUtil;
import lombok.Data;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.util.CollectionUtils;

import java.util.Map;

/**
 * Packaging the creation process of orbit advice.
 *
 * @since 3.15.0
 * @author yizzuide
 * <br>
 * Create at 2023/01/27 21:12
 */
@Data
public class OrbitAdviceFactoryBean implements FactoryBean<OrbitAdvice> {

    /**
     * Orbit advice type.
     */
    private Class<? extends OrbitAdvice> adviceClass;

    /**
     * Orbit advice property values.
     */
    private Map<String, Object> adviceProps;

    /**
     * Spring context.
     */
    private ConfigurableListableBeanFactory beanFactory;

    @SuppressWarnings("unchecked")
    @Override
    public OrbitAdvice getObject() throws Exception {
        // 使用ObjectProvider的延迟查找代替getBean()的立即查找，因为getBean()找不到会有异常
        ObjectProvider<OrbitAdvice> adviceProvider = (ObjectProvider<OrbitAdvice>) beanFactory.getBeanProvider(this.getAdviceClass());
        OrbitAdvice advice = adviceProvider.getIfAvailable(() -> ReflectUtil.newInstance(this.getAdviceClass()));
        // autowire it's fields!
        beanFactory.autowireBean(advice);
        // reflect and set custom props
        if (!CollectionUtils.isEmpty(this.getAdviceProps())) {
            ReflectUtil.setField(advice, this.getAdviceProps());
        }
        return advice;
    }

    @Override
    public Class<?> getObjectType() {
        return OrbitAdvice.class;
    }
}
