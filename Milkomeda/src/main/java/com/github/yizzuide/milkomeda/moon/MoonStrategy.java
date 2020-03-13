package com.github.yizzuide.milkomeda.moon;

/**
 * MoonStrategy
 *
 * @author yizzuide
 * @since 2.6.0
 * Create at 2020/03/13 21:16
 */
public interface MoonStrategy {

    /**
     * 无序并发获得当前阶段类型（不支持分布式）
     * @param moon Moon
     * @return 阶段类型值
     */
    <T> T getCurrentPhase(Moon<T> moon);

    /**
     * 根据key获取当前轮的当前阶段的数据值（分布式方式）
     * @param key          缓存key，一个轮对应一个key
     * @param p            当前阶段值
     * @param prototype    Moon实例原型
     * @param <T>          阶段的类型
     * @return  当前轮的当前阶段的类型值
     */
    <T> T getPhase(String key, Integer p, Moon<T> prototype);

    /**
     * 拔动月相
     * @param moon Moon
     * @param leftHandPointer LeftHandPointer
     * @return LeftHandPointer
     */
    LeftHandPointer pluck(Moon<?> moon, LeftHandPointer leftHandPointer);
}
