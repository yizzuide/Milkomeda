package com.github.yizzuide.milkomeda.metal;

import java.util.HashMap;
import java.util.Map;

/**
 * MetalConfigSource
 * 配置源
 *
 * @author yizzuide
 * @since 3.6.0
 * Create at 2020/05/21 18:26
 */
public class MetalSource {

    private Map<String, String> sourceMap = new HashMap<>();

    public void putAll(Map<String, String> sourceMap) {
        this.sourceMap = sourceMap;
    }

    public void put(String key, String value) {
        this.sourceMap.put(key, value);
    }

    public String get(String key) {
        return this.sourceMap.get(key);
    }
}
