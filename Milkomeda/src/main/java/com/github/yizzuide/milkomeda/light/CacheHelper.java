package com.github.yizzuide.milkomeda.light;

import com.fasterxml.jackson.core.type.TypeReference;
import com.github.yizzuide.milkomeda.util.TypeUtil;
import lombok.extern.slf4j.Slf4j;

import java.util.function.Function;

/**
 * CacheHelper
 *
 * 缓存帮助类
 *
 * @since 1.10.0
 * @version 1.10.2
 * @author yizzuide
 * Create at 2019/07/02 11:36
 */
@Slf4j
public class CacheHelper {

    /**
     * 设置标识数据，用于超级缓存
     * @param cache     缓存类
     * @param id        标识值
     * @param <V>       标识类型
     * @param <E>       数据类型
     */
    public <V, E> void set(Cache<V, E> cache, V id) {
        cache.set(id);
    }

    /**
     * 从缓存获取数据，支持一级缓存、二级缓存（如果使用默认配置的话）
     * @param cache             缓存类
     * @param vClazz            标识Class
     * @param eClazz            数据Class
     * @param id                标识值
     * @param keyGenerator      缓存key产生器
     * @param dataGenerator     数据产生器
     * @param <V>               标识类型
     * @param <E>               数据类型
     * @return                  缓存数据
     */
    public static  <V, E> E get(Cache<V, E> cache, Class<V> vClazz, Class<E> eClazz, V id,
                                Function<V, String> keyGenerator, Function<V, E> dataGenerator) {
        return get(cache, TypeUtil.class2TypeRef(vClazz), TypeUtil.class2TypeRef(eClazz), id, keyGenerator, dataGenerator);
    }

    /**
     * 从缓存获取数据，支持超级缓存、一级缓存、二级缓存（如果使用默认配置的话）
     *
     * 注意：超级缓存需要提前设置标识数据
     *
     * @param cache             缓存类
     * @param vClazz            标识Class
     * @param eClazz            数据Class
     * @param keyGenerator      缓存key产生器
     * @param dataGenerator     数据产生器
     * @param <V>               标识类型
     * @param <E>               数据类型
     * @return                  缓存数据
     */
    public static  <V, E> E get(Cache<V, E> cache, Class<V> vClazz, Class<E> eClazz,
                                Function<V, String> keyGenerator, Function<V, E> dataGenerator) {
        return get(cache, TypeUtil.class2TypeRef(vClazz), TypeUtil.class2TypeRef(eClazz), keyGenerator, dataGenerator);
    }

    /**
     * 从缓存获取数据，支持一级缓存、二级缓存（如果使用默认配置的话）
     * @param cache             缓存类
     * @param vTypeRef          标识TypeReference
     * @param eTypeRef          数据TypeReference
     * @param id                标识值
     * @param keyGenerator      缓存key产生器
     * @param dataGenerator     数据产生器
     * @param <V>               标识类型
     * @param <E>               数据类型
     * @return                  缓存数据
     */
    public static  <V, E> E get(Cache<V, E> cache, TypeReference<V> vTypeRef, TypeReference<E> eTypeRef,
                                V id, Function<V, String> keyGenerator, Function<V, E> dataGenerator) {
        String key = keyGenerator.apply(id);
        Spot<V, E> spot = cache.get(key, vTypeRef, eTypeRef);
        if (spot == null) {
            E data = dataGenerator.apply(id);
            spot = new Spot<>(id, data);
            // 一级缓存池获取（耗时为O(logN)），或二级缓存Redis获取（耗时：网络+序列化）
            cache.set(key, spot);
            return data;
        }
        return spot.getData();
    }

    /**
     * 从缓存获取数据，支持超级缓存、一级缓存、二级缓存（如果使用默认配置的话）
     *
     * 注意：超级缓存需要提前设置标识数据
     *
     * @param cache             缓存类
     * @param vTypeRef          标识TypeReference
     * @param eTypeRef          数据TypeReference
     * @param keyGenerator      缓存key产生器
     * @param dataGenerator     数据产生器
     * @param <V>               标识类型
     * @param <E>               数据类型
     * @return                  缓存数据
     */
    public static  <V, E> E get(Cache<V, E> cache, TypeReference<V> vTypeRef, TypeReference<E> eTypeRef,
                                Function<V, String> keyGenerator, Function<V, E> dataGenerator) {
        // 方案一：从超级缓存中获取，内存指针引用即可返回（耗时为O(1)，速度快到没朋友）
        Spot<V, E> fastSpot = cache.get();
        if (fastSpot == null) {
            log.error("CacheHelper:- 超级缓存设置错误，没有获取标识数据");
            return null;
        }

        // 获取业务数据
        E data = fastSpot.getData();
        if (data != null) {
            return data;
        }

        // 设置缓存key
        String key = keyGenerator.apply(fastSpot.getView());

        // 方案二：直接尝试从一级缓存池获取（耗时为O(logN)），或二级缓存Redis获取（耗时：网络+序列化）
        Spot<V, E> spot = cache.get(key, vTypeRef, eTypeRef);
        if (spot != null) {
            data = spot.getData();
            // 设置到超级缓存
            fastSpot.setData(data);
            return data;
        }

        // 从数据库获取（耗时最长，一个标识只会查一次）
        data = dataGenerator.apply(fastSpot.getView());
        // 设置到超级缓存
        fastSpot.setData(data);
        // 一级缓存（内存，默认缓存64个，超出时使用热点旧数据丢弃策略） -> 二级缓存（Redis）
        cache.set(key, fastSpot);
        return data;
    }

}
