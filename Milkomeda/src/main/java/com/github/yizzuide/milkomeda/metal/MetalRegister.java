/*
 * Copyright (c) 2021 yizzuide All rights Reserved.
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

package com.github.yizzuide.milkomeda.metal;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.InstantiationAwareBeanPostProcessorAdapter;
import org.springframework.lang.NonNull;

import java.lang.reflect.Field;

/**
 * MetalRegister
 * 注册虚拟节点
 *
 * @author yizzuide
 * @since 3.6.0
 * Create at 2020/05/21 18:31
 */
@Slf4j
public class MetalRegister extends InstantiationAwareBeanPostProcessorAdapter {
    /**
     * 容器
     */
    private final MetalContainer metalContainer;

    public MetalRegister(MetalContainer metalContainer) {
        this.metalContainer = metalContainer;
    }

    @Override
    public boolean postProcessAfterInstantiation(@NonNull Object bean, @NonNull String beanName) throws BeansException {
        try {
            Class<?> clz = bean.getClass();
            Metal metaVal;
            for (Field field : clz.getDeclaredFields()) {
                metaVal = field.getAnnotation(Metal.class);
                if (metaVal != null) {
                    metalContainer.addVNode(metaVal, bean, field);
                }
            }
        } catch (Exception e) {
            log.error("Metal process post bean error with msg: {}", e.getMessage(), e);
        }
        return super.postProcessAfterInstantiation(bean, beanName);
    }
}
