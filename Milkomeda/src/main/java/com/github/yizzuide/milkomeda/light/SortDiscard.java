package com.github.yizzuide.milkomeda.light;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * SortDiscard
 *
 * 抽象的字段排序方案
 *
 * @since 1.8.0
 * @author yizzuide
 * Create at 2019/06/28 16:32
 */
@Slf4j
public abstract class SortDiscard<V, E> implements Discard<V, E> {

    @Override
    @SuppressWarnings("unchecked")
    public Spot<V, E> deform(String key, Spot<V, E> spot) {
        SortSpot<V, E> sortSpot = null;
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
    public void discard(Map<String, Spot<V, E>> cacheMap, float l1DiscardPercent) {
        List<? extends SortSpot<V, E>> list = ((Collection<? extends SortSpot<V, E>>) cacheMap.values())
                .stream()
                .sorted((Comparator<? super SortSpot<V, E>>) comparator())
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
    protected abstract Comparator<? extends SortSpot<V, E>> comparator();
}
