package com.github.yizzuide.milkomeda.metal;

import com.github.yizzuide.milkomeda.util.ReflectUtil;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.CollectionUtils;

import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * MetalContainer
 *
 * @author yizzuide
 * Create at 2020/05/21 18:36
 */
@Slf4j
public class MetalContainer {

    private final MetalConfigSource metalConfigSource;

    // 保存配置与Field之间的绑定关系
    private final Map<String, Set<InvokeCell>> metaCache = new ConcurrentHashMap<>();

    public MetalContainer(MetalConfigSource metalConfigSource) {
        this.metalConfigSource = metalConfigSource;
    }

    public String getProperty(String key) {
        return metalConfigSource.get(key);
    }

    // 用于新增绑定关系并初始化
    public void addInvokeCell(Metal metal, Object target, Field field) throws IllegalAccessException {
        String key = metal.value();
        if (!metaCache.containsKey(key)) {
            synchronized (this) {
                if (!metaCache.containsKey(key)) {
                    metaCache.put(key, new HashSet<>());
                }
            }
        }
        metaCache.get(key).add(new InvokeCell(metal, target, field, getProperty(key)));
    }

    // 配置更新
    public void updateMetal(String key, String oldVal, String newVal) {
        Set<InvokeCell> cacheSet = metaCache.get(key);
        if (CollectionUtils.isEmpty(cacheSet)) {
            return;
        }

        cacheSet.forEach(s -> {
            try {
                s.update(newVal);
                log.info("update {} from {} to {}", s.getSignature(), oldVal, newVal);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        });
    }

    @Data
    public static class InvokeCell {
        private Metal metal;

        private Object target;

        private Field field;

        private String signature;

        private Object value;

        public InvokeCell(Metal metal, Object target, Field field, String value) throws IllegalAccessException {
            this.metal = metal;
            this.target = target;
            this.field = field;
            field.setAccessible(true);
            signature = target.getClass().getName() + "." + field.getName();
            update(value);
        }

        public void update(String value) throws IllegalAccessException {
            this.value = ReflectUtil.setTypeField(field, value);
            field.set(target, this.value);
        }
    }
}
