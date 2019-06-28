package com.github.yizzuide.milkomeda.light;

import java.util.Map;

/**
 * Discard
 *
 * 缓存数据丢弃策略接口
 *
 * @since 1.8.0
 * @author yizzuide
 * Create at 2019/06/28 14:50
 */
public interface Discard<V, E> {

    /**
     * 返回具体的缓存数据类
     *
     * @return 缓存数据类
     */
    Class<? extends SortSpot> spotClazz();

    /**
     * 转型
     *
     * @param key   缓存key
     * @param spot  缓存数据
     * @return  Spot
     */
    Spot<V, E> deform(String key, Spot<V, E> spot);

    /**
     * 提升缓存数据的权重
     *
     * @param spot  缓存数据
     */
    void ascend(Spot<V, E> spot);

    /**
     * 丢弃缓存数据
     * @param cacheMap          缓存容器
     * @param l1DiscardPercent  丢弃百分数
     */
    void discard(Map<String, Spot<V, E>> cacheMap, float l1DiscardPercent);
}
