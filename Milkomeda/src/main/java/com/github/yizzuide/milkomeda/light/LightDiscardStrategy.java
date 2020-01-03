package com.github.yizzuide.milkomeda.light;

/**
 * LightDiscardStrategy
 *
 * @author yizzuide
 * @version 1.17.0
 * @version 2.0.1
 * Create at 2019/12/03 16:30
 */
public enum LightDiscardStrategy {
    /**
     * 默认策略（默认为HOT）
     */
    DEFAULT,
    /**
     * 热点策略，LFU（适用于大量相同类型记录数据的情况，使用得越频繁越不容易过期）
     */
    HOT,
    /**
     * 时间线策略，LRU（适用于大量相同类型记录数据的情况，最新使用的数据不会被丢弃）
     */
    TIMELINE,
    /**
     * 懒惰过期丢弃策略（轻量级过期方案，适用于缓存数据会在一定时间会被更新的情况）
     */
    LazyExpire,
    /**
     * 自定义策略
     */
    CUSTOM
}
