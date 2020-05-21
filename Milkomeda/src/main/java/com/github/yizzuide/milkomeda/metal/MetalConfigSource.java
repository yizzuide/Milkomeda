package com.github.yizzuide.milkomeda.metal;

import java.util.HashMap;
import java.util.Map;

/**
 * MetalConfigSource
 *
 * @author yizzuide
 * @since 3.6.0
 * Create at 2020/05/21 18:26
 */
public class MetalConfigSource {

    private Map<String, String> sourceMap = new HashMap<>();

    public void putAll(Map<String, String> sourceMap) {
        this.sourceMap = sourceMap;
    }

    public void put(String key, String value) {
        this.sourceMap.put(key, value);
    }

    public String get(String key) {
        return key;
    }
}
