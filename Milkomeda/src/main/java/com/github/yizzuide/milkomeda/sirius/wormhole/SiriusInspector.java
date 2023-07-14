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

package com.github.yizzuide.milkomeda.sirius.wormhole;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.jetbrains.annotations.NotNull;
import org.mybatis.spring.mapper.MapperFactoryBean;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.SmartInstantiationAwareBeanPostProcessor;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

/**
 * An inspector for get mapper dynamically to mybatis and mybatis-plus.
 *
 * @see org.springframework.transaction.support.TransactionSynchronizationManager
 * @see org.apache.ibatis.session.SqlSession
 * @see org.apache.ibatis.session.SqlSessionManager
 * @see com.baomidou.mybatisplus.core.MybatisMapperRegistry
 * @see org.mybatis.spring.SqlSessionUtils
 * @see com.baomidou.mybatisplus.extension.toolkit.SqlHelper
 * @see com.baomidou.mybatisplus.core.toolkit.GlobalConfigUtils
 *
 * @since 3.15.0
 * @author yizzuide
 * Create at 2023/07/09 15:52
 */
public class SiriusInspector implements SmartInstantiationAwareBeanPostProcessor {

    private final static Map<String, BaseMapper<?>> mapperInstanceTypes = new HashMap<>();

    private final static Map<String, String> mapperBeanNameTypes = new HashMap<>();

    @Override
    public Object postProcessBeforeInitialization(@NotNull Object bean, @NotNull String beanName) throws BeansException {
        if (bean instanceof MapperFactoryBean) {
            Class<?> mapperInterface = ((MapperFactoryBean<?>) bean).getMapperInterface();
            Type[] actualTypeArguments = ((ParameterizedType) (mapperInterface.getGenericInterfaces())[0])
                    .getActualTypeArguments();
            String typeName = actualTypeArguments[0].getTypeName();
            mapperBeanNameTypes.put(beanName, typeName);
        }
        return SmartInstantiationAwareBeanPostProcessor.super.postProcessBeforeInitialization(bean, beanName);
    }

    @Override
    public Object postProcessAfterInitialization(@NotNull Object bean, @NotNull String beanName) throws BeansException {
        if (bean instanceof  BaseMapper) {
            String typeName = mapperBeanNameTypes.get(beanName);
            mapperInstanceTypes.put(typeName, (BaseMapper<?>) bean);
        }
        return SmartInstantiationAwareBeanPostProcessor.super.postProcessAfterInitialization(bean, beanName);
    }

    /**
     * Get mapper from entity class.
     * @param entityClass   entity class
     * @return  mapper instance
     * @param <T>   entity type
     */
    @SuppressWarnings("unchecked")
    public static  <T> BaseMapper<T> getMapper(Class<T> entityClass) {
        return (BaseMapper<T>) mapperInstanceTypes.get(entityClass.getName());
    }

}
