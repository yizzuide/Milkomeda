package com.github.yizzuide.milkomeda.moon;

/**
 * MoonStrategy
 *
 * @author yizzuide
 * @since 2.6.0
 * @version 3.7.0
 * Create at 2020/03/13 21:16
 */
public interface MoonStrategy {

    /**
     * 无序并发获得当前阶段类型（不支持分布式）
     * @param moon Moon
     * @param <T> 阶段类型
     * @return 阶段值
     */
    <T> T getCurrentPhase(Moon<T> moon);

    /**
     * 获得环值（分布式并发安全）
     * @param key          缓存key，一个环对应一个key
     * @param p            当前阶段值
     * @param prototype    Moon实例原型
     * @param <T>          阶段的类型
     * @return  当前环的当前阶段值
     */
    <T> T getPhase(String key, Integer p, Moon<T> prototype);

    /**
     * 拔动月环
     * @param moon Moon
     * @param leftHandPointer LeftHandPointer
     * @return LeftHandPointer
     */
    LeftHandPointer pluck(Moon<?> moon, LeftHandPointer leftHandPointer);

    /**
     * 执行lua流程脚本获得环值
     * @param key       缓存key，一个环对应一个key  
     * @param prototype Moon实例原型
     * @param <T>       阶段的类型
     * @return  当前环的当前阶段值
     * @since 3.7.0
     */
    <T> T getPhaseFast(String key, Moon<T> prototype);
}
