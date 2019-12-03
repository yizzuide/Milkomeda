package com.github.yizzuide.milkomeda.light;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;

import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;

/**
 * SortDiscard
 *
 * 抽象的字段排序方案
 *
 * @since 1.8.0
 * @version 1.17.0
 * @author yizzuide
 * Create at 2019/06/28 16:32
 */
@Slf4j
public abstract class SortDiscard implements Discard {

    @Override
    @SuppressWarnings("unchecked")
    public Spot<Serializable, Object> deform(String key, Spot<Serializable, Object> spot) {
        SortSpot<Serializable, Object> sortSpot = null;
        try {
            sortSpot = spotClazz().newInstance();
            BeanUtils.copyProperties(spot, sortSpot);
            sortSpot.setKey(key);
        } catch (Exception e) {
            log.error("SortDiscard:- 创建类实例失败：{}", e.getMessage(), e);
        }
        return sortSpot;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void discard(Map<String, Spot<Serializable, Object>> cacheMap, float l1DiscardPercent) {
        List<? extends SortSpot<Serializable, Object>> list = cacheMap.values()
                .stream()
                .map(spot -> (SortSpot<Serializable, Object>)spot)
                .sorted((Comparator<? super SortSpot<Serializable, Object>>) comparator())
                .collect(Collectors.toList());
        int discardCount = Math.round(list.size() * l1DiscardPercent);
        // 一级缓存百分比太小，直接返回
        if (discardCount == 0) {
            return;
        }
        if (discardCount == list.size()) {
            discardCount--;
        }
        for (int i = 0; i <= discardCount; i++) {
            String key = list.get(i).getKey();
            cacheMap.remove(key);
        }
    }

    /**
     * 排序比较器
     * @return Comparator
     */
    protected abstract Comparator<? extends SortSpot<Serializable, Object>> comparator();
}
