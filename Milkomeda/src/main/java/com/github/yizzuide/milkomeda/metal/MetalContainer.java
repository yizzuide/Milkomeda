package com.github.yizzuide.milkomeda.metal;

import com.github.yizzuide.milkomeda.util.ReflectUtil;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.StringUtils;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * MetalContainer
 * 配置容器，不推荐直接使用该类API，因为有线程安全问题，该使用 {@link MetalHolder}
 *
 * @author yizzuide
 * @since 3.6.0
 * Create at 2020/05/21 18:36
 */
@Slf4j
public class MetalContainer {

    private final MetalSource metalSource;

    private final Map<String, Set<VirtualNode>> vNodeCache = new HashMap<>();

    public MetalContainer(MetalSource metalSource) {
        this.metalSource = metalSource;
    }

    /**
     * 初始化配置源
     * @param source 配置源
     */
    public void init(Map<String, String> source) {
        metalSource.putAll(source);
        for (Map.Entry<String, String> entry : source.entrySet()) {
            updateVNode(entry.getKey(), entry.getValue());
        }
    }

    /**
     * 获取配置值
     * @param key   配置key
     * @return  值
     */
    public String getProperty(String key) {
        return metalSource.get(key);
    }

    /**
     * 添加虚拟节点
     * @param metal     Metal元信息
     * @param target    目标
     * @param field     属性
     */
    public void addVNode(Metal metal, Object target, Field field) {
        String key = metal.value();
        if (StringUtils.isEmpty(key)) {
            key = field.getName();
        }
        if (!vNodeCache.containsKey(key)) {
            vNodeCache.put(key, new HashSet<>());
        }
        // key与虚拟节点绑定
        vNodeCache.get(key).add(new VirtualNode(metal, target, field));
    }

    /**
     * 更新虚拟节点
     * @param key       绑定key
     * @param newVal    新值
     */
    public void updateVNode(String key, String newVal) {
        String oldVal = metalSource.get(key);
        Set<VirtualNode> cacheSet = vNodeCache.get(key);
        if (CollectionUtils.isEmpty(cacheSet)) {
            return;
        }
        for (VirtualNode vNode : cacheSet) {
            vNode.update(newVal);
            metalSource.put(key, newVal);
            log.debug("Metal update vnode {} with key {} from old value {} to {}", vNode.getSignature(), key, oldVal, newVal);
        }
    }

    @Data
    public static class VirtualNode {
        private Metal metal;
        private Object target;
        private Field field;
        private String signature;
        private Object value;

        public VirtualNode(Metal metal, Object target, Field field) {
            this.metal = metal;
            this.target = target;
            this.field = field;
            signature = target.getClass().getName() + "." + field.getName();
            field.setAccessible(true);
        }

        public void update(String value) {
            this.value = ReflectUtil.setTypeField(field, value);
            ReflectionUtils.setField(field, target, this.value);
        }
    }
}
