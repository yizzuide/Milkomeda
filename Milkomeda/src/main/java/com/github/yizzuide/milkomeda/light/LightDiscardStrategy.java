package com.github.yizzuide.milkomeda.light;

/**
 * LightDiscardStrategy
 *
 * @author yizzuide
 * @version 1.17.0
 * Create at 2019/12/03 16:30
 */
public enum LightDiscardStrategy {
    /**
     * 热点策略，使用得越频繁，越不容易过期
     */
    HOT,
    /**
     * 时间线策略，丢弃过去使用的数据，保存最新添加的数据
     */
    TIMELINE,
    /**
     * 自定义策略
     */
    CUSTOM
}
