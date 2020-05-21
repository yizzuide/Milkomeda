package com.github.yizzuide.milkomeda.metal;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.InstantiationAwareBeanPostProcessorAdapter;
import org.springframework.lang.NonNull;

import java.lang.reflect.Field;

/**
 * MetalRegister
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
