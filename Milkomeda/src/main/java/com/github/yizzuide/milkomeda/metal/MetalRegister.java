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
 * Create at 2020/05/21 18:31
 */
@Slf4j
public class MetalRegister extends InstantiationAwareBeanPostProcessorAdapter {

    private final MetalContainer metalContainer;

    public MetalRegister(MetalContainer metalContainer) {
        this.metalContainer = metalContainer;
    }

    @Override
    public boolean postProcessAfterInstantiation(@NonNull Object bean, @NonNull String beanName) throws BeansException {
        processMetal(bean);
        return super.postProcessAfterInstantiation(bean, beanName);
    }

    private void processMetal(Object bean) {
        try {
            Class<?> clz = bean.getClass();
            Metal metaVal;
            for (Field field : clz.getDeclaredFields()) {
                metaVal = field.getAnnotation(Metal.class);
                if (metaVal != null) {
                    // 缓存配置与Field的绑定关系，并初始化
                    metalContainer.addInvokeCell(metaVal, bean, field);
                }
            }
        } catch (Exception e) {
            log.error("Metal process post bean error with msg: {}", e.getMessage(), e);
        }
    }
}
