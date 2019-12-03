package com.github.yizzuide.milkomeda.light;

import com.fasterxml.jackson.core.type.TypeReference;
import com.github.yizzuide.milkomeda.util.TypeUtil;
import lombok.extern.slf4j.Slf4j;

import java.io.Serializable;
import java.util.function.Function;

/**
 * CacheHelper
 *
 * 缓存帮助类
 *
 * @since 1.10.0
 * @version 1.17.0
 * @author yizzuide
 * Create at 2019/07/02 11:36
 */
@Slf4j
public class CacheHelper {

    /**
     * 设置超级缓存的缓存标识数据 {@link LightContext}
     * @param cache     缓存实例
     * @param id        标识值
     */
    public static void set(Cache cache, Serializable id) {
        cache.set(id);
    }

    /**
     * 设置超级缓存的缓存数据 {@link LightContext}
     * @param cache     缓存实例
     * @param spot      缓存数据
     */
    public static void set(Cache cache, Spot<Serializable, ?> spot) {
        if (cache instanceof LightCache) {
            LightCache lightCache = (LightCache) cache;
            lightCache.getSuperCache().set(spot);
        }
    }

    /**
     * 从超级缓存获取数据 {@link LightContext}
     * @param cache     缓存实例
     * @return          Spot
     */
    public static <E> Spot<Serializable, E> get(Cache cache) {
        return cache.get();
    }
    
    /**
     * 移除超级缓存的数据 {@link LightContext}
     * @param cache     缓存实例
     */
    public static void remove(Cache cache) {
        cache.remove();
    }

    /**
     * 从缓存获取数据，支持一级缓存、二级缓存（如果使用默认配置的话）
     * @param cache             缓存实例
     * @param eClazz            数据Class
     * @param id                标识值
     * @param keyGenerator      缓存key产生器
     * @param dataGenerator     数据产生器
     * @return                  缓存数据
     */
    public static <E> E get(Cache cache, Class<E> eClazz, Serializable id,
                                Function<Serializable, String> keyGenerator, Function<String, E> dataGenerator) {
        return get(cache, TypeUtil.class2TypeRef(eClazz), id, keyGenerator, dataGenerator);
    }

    /**
     * 从缓存获取数据，支持超级缓存、一级缓存、二级缓存（如果使用默认配置的话）
     *
     * 注意：超级缓存需要提前设置标识数据
     *
     * @param cache             缓存实例
     * @param eClazz            数据Class
     * @param keyGenerator      缓存key产生器
     * @param dataGenerator     数据产生器
     * @return                  缓存数据
     */
    public static  <E>  E get(Cache cache, Class<E> eClazz,
                                Function<Serializable, String> keyGenerator, Function<String, E> dataGenerator) {
        return get(cache, TypeUtil.class2TypeRef(eClazz), keyGenerator, dataGenerator);
    }

    /**
     * 从缓存获取数据，支持一级缓存、二级缓存（如果使用默认配置的话）
     * @param cache             缓存实例
     * @param eTypeRef          数据TypeReference
     * @param id                标识值
     * @param keyGenerator      缓存key产生器
     * @param dataGenerator     数据产生器
     * @return                  缓存数据
     */
    public static <E> E get(Cache cache, TypeReference<E> eTypeRef,
                                Serializable id, Function<Serializable, String> keyGenerator, Function<String, E> dataGenerator) {
        String key = keyGenerator.apply(id);
        // 一级缓存池获取（耗时为O(logN)），或二级缓存Redis获取（耗时：网络+序列化）
        Spot<Serializable, E> spot = cache.get(key, new TypeReference<Serializable>() {}, eTypeRef);
        if (spot == null) {
            E data = dataGenerator.apply(String.valueOf(id));
            spot = new Spot<>(id, data);
            // 放入缓存中
            cache.set(key, spot);
            return data;
        }
        // 返回缓存中数据
        return spot.getData();
    }

    /**
     * 从缓存获取数据，支持超级缓存、一级缓存、二级缓存（如果使用默认配置的话）
     *
     * 注意：超级缓存需要提前设置标识数据
     *
     * @param cache             缓存实例
     * @param eTypeRef          数据TypeReference
     * @param keyGenerator      缓存key产生器
     * @param dataGenerator     数据产生器
     * @return                  缓存数据
     */
    public static  <E> E get(Cache cache, TypeReference<E> eTypeRef,
                                Function<Serializable, String> keyGenerator, Function<String, E> dataGenerator) {
        // 方案一：从超级缓存中获取，内存指针引用即可返回（耗时为O(1)，速度快到没朋友）
        Spot<Serializable, E> fastSpot = cache.get();
        if (fastSpot == null) {
            log.error("CacheHelper:- 超级缓存设置错误，没有获取到标识数据");
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
        Spot<Serializable, E> spot = cache.get(key, new TypeReference<Serializable>() {}, eTypeRef);
        if (spot != null) {
            data = spot.getData();
            // 设置到超级缓存
            fastSpot.setData(data);
            return data;
        }

        // 从数据库获取（耗时最长，一个标识只会查一次）
        data = dataGenerator.apply(fastSpot.getView().toString());
        // 设置到超级缓存
        fastSpot.setData(data);
        // 一级缓存（内存，默认缓存64个，超出时使用热点旧数据丢弃策略） -> 二级缓存（Redis）
        cache.set(key, fastSpot);
        return data;
    }

    /**
     * 擦除指定缓存数据（不管有没有用上超级缓存，只需要删除一级缓存和二级缓存）
     * @param cache         缓存实例
     * @param id            标识值
     * @param keyGenerator  缓存key生成器
     */
    public static  <E> void erase(Cache cache, Serializable id, Function<Serializable, String> keyGenerator) {
        cache.erase(keyGenerator.apply(id));
    }
}
