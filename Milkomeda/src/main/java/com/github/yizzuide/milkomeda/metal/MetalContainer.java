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

import com.github.yizzuide.milkomeda.util.DataTypeConvertUtil;
import com.github.yizzuide.milkomeda.util.ReflectUtil;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ReflectionUtils;
import com.github.yizzuide.milkomeda.util.Strings;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * MetalContainer
 * 配置容器，不推荐直接使用该类API，因为有线程安全问题，应该使用 {@link MetalHolder}
 *
 * @author yizzuide
 * @since 3.6.0
 * @version 3.7.0
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
            updateVNode(entry.getKey(), null, entry.getValue());
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
        if (Strings.isEmpty(key)) {
            key = field.getName();
        }
        String vNodeKey = key;
        if (vNodeKey.contains("_")) {
            vNodeKey = DataTypeConvertUtil.toCamelCase(vNodeKey);
        }
        if (!vNodeCache.containsKey(vNodeKey)) {
            vNodeCache.put(vNodeKey, new HashSet<>());
        }
        // key与虚拟节点绑定，一个key对应多个节点
        vNodeCache.get(vNodeKey).add(new VirtualNode(metal, target, field));
    }

    /**
     * 更新虚拟节点
     * @param key       绑定key
     * @param oldVal    旧值
     * @param newVal    新值
     */
    public void updateVNode(String key, String oldVal, String newVal) {
        metalSource.put(key, newVal);
        String vNodeKey = key;
        if (vNodeKey.contains("_")) {
            vNodeKey = DataTypeConvertUtil.toCamelCase(vNodeKey);
        }
        Set<VirtualNode> cacheSet = vNodeCache.get(vNodeKey);
        if (CollectionUtils.isEmpty(cacheSet)) {
            return;
        }
        for (VirtualNode vNode : cacheSet) {
            vNode.update(newVal);
            log.info("Metal update vnode '{}' with key '{}' from old value '{}' to '{}'", vNode.getSignature(), key, oldVal, newVal);
        }
    }

    /**
     * 获取配置源
     * @return MetalSource
     */
    public MetalSource getSource() {
        return metalSource;
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
            this.value = ReflectUtil.getTypeValue(field, value);
            ReflectionUtils.setField(field, target, this.value);
        }
    }
}
