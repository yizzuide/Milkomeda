package com.github.yizzuide.milkomeda.light;


import com.fasterxml.jackson.core.type.TypeReference;

/**
 * Cache
 * 缓存接口
 *
 * V：标识数据
 * E：缓存业务数据
 *
 * @since 1.9.0
 * @author yizzuide
 * Create at 2019/07/01 15:39
 */
public interface Cache<V, E> {
    /**
     * 设置超级缓存
     *
     * @param id    缓存标识符
     */
    void set(V id);

    /**
     * 获取超级缓存数据
     *
     * @return  Spot
     */
    Spot<V, E> get();

    /**
     * 清除超级缓存
     */
    void remove();

    /**
     * 存入缓存
     * @param key       键
     * @param spot      缓存数据
     */
    void set(String key, Spot<V, E> spot);

    /**
     * 从缓存获取
     * <br>
     * 注意：这个简单方法只支持基本类型的包装类型以及String，其它类型请不要使用
     *
     * @param key   键
     * @return      Spot
     * @deprecated  1.9.0版本开始标为过时
     */
    @Deprecated
    Spot<V, E> get(String key);

    /**
     * 从缓存获取
     *
     * @param key       键
     * @param vClazz    标识数据类型
     * @param eClazz    业务数据类型
     * @return          Spot
     */
    Spot<V, E> get(String key, Class<V> vClazz, Class<E> eClazz);

    /**
     * 从缓存获取
     *
     * @param key       键
     * @param vTypeRef  标识数据TypeReference
     * @param eTypeRef  业务数据TypeReference
     * @return          Spot
     */
    Spot<V, E> get(String key, TypeReference<V> vTypeRef, TypeReference<E> eTypeRef);
}
