/*
 * Copyright (c) 2021 yizzuide All rights Reserved.
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.github.yizzuide.milkomeda.light;

import com.fasterxml.jackson.core.type.TypeReference;
import com.github.yizzuide.milkomeda.atom.Atom;
import com.github.yizzuide.milkomeda.atom.AtomHolder;
import com.github.yizzuide.milkomeda.atom.AtomLockType;
import com.github.yizzuide.milkomeda.universe.function.ThrowableFunction;
import com.github.yizzuide.milkomeda.util.ReflectUtil;
import com.github.yizzuide.milkomeda.util.TypeUtil;
import lombok.extern.slf4j.Slf4j;

import java.io.Serializable;
import java.util.function.Function;

/**
 * 缓存外层API，集超级缓存、一级缓存、二级缓存于一体的方法
 *
 * @since 1.10.0
 * @version 3.20.0
 * @author yizzuide
 * <br>
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
    public static void set(Cache cache, Spot<Serializable, Object> spot) {
        LightCache lightCache = (LightCache) cache;
        lightCache.getSuperCache().set(spot);
    }

    /**
     * 从超级缓存获取数据 {@link LightContext}
     * @param cache     缓存实例
     * @param <E> 实体类型
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
     * 设置并获取超级缓存数据
     * @param cache             缓存实例
     * @param dataGenerator     数据产生器
     * @param <E>               实体类型
     * @return                  缓存数据
     * @since 3.12.10
     */
    @SuppressWarnings("unchecked")
    public static <E> E getFastLevel(Cache cache, Function<Spot<Serializable, E>, E> dataGenerator) {
        E data = null;
        Spot<Serializable, E> fastSpot;
        if (cache instanceof LightCache && ((LightCache) cache).isEnableSuperCache()) {
            fastSpot = CacheHelper.get(cache);
            if (fastSpot != null && fastSpot.getData() != null) {
                return fastSpot.getData();
            }
            if (fastSpot == null) {
                fastSpot = new Spot<>();
            }
            data = dataGenerator.apply(fastSpot);
            fastSpot.setData(data);
            set(cache, (Spot<Serializable, Object>)fastSpot);
        }
        return data;
    }

    /**
     * 从缓存获取数据，支持超级缓存、一级缓存、二级缓存（如果使用默认配置的话）
     * @param cache         缓存实例
     * @param eClazz        数据Class
     * @param id            标识值
     * @param keyGenerator  缓存key产生器
     * @param dataGenerator 数据产生器
     * @param <E>   实体类型
     * @return  缓存数据
     * @throws Throwable 获取异常
     */
    public static <E> E get(Cache cache, Class<E> eClazz, Serializable id,
                            Function<Serializable, String> keyGenerator, ThrowableFunction<String, E> dataGenerator) throws Throwable {
        return get(cache, TypeUtil.class2TypeRef(eClazz), id, keyGenerator, dataGenerator);
    }

    /**
     * 从缓存获取数据，支持超级缓存、一级缓存、二级缓存（如果使用默认配置的话）
     *
     * @param cache             缓存实例
     * @param eTypeRef          数据TypeReference
     * @param id                标识值
     * @param keyGenerator      缓存key产生器
     * @param dataGenerator     数据产生器
     * @param <E>               实体类型
     * @return                  缓存数据
     * @throws Throwable 获取异常
     */
    @SuppressWarnings("unchecked")
    public static <E> E get(Cache cache, TypeReference<E> eTypeRef, Serializable id,
                            Function<Serializable, String> keyGenerator, ThrowableFunction<String, E> dataGenerator) throws Throwable {
        E data;
        Spot<Serializable, E> fastSpot;
        LightCache lightCache = (LightCache) cache;
        if (lightCache.isEnableSuperCache() && !lightCache.isOnlyCacheL2()) {
            // 方案一：从超级缓存中获取，内存指针引用即可返回（耗时为O(1)）
            fastSpot = get(cache);
            if (fastSpot != null) {
                data = fastSpot.getData();
                if (data != null && id.equals(fastSpot.getView())) {
                    return data;
                }
            } else {
                // 设置超级缓存
                set(cache, id);
                fastSpot = get(cache);
            }
        } else {
            fastSpot = new Spot<>();
            fastSpot.setView(id);
        }

        // 设置缓存key
        String key = keyGenerator.apply(fastSpot.getView());

        // 方案二：直接尝试从一级缓存池获取（耗时为O(logN)），或二级缓存Redis获取（耗时：网络+序列化）
        Spot<Serializable, E> spot = cache.get(key, new TypeReference<Serializable>() {}, eTypeRef);
        if (spot != null) {
            data = spot.getData();
            // 设置缓存数据
            fastSpot.setData(data);
            return data;
        }

        // 方案三：读取网络数据源时，只有一个线程能获取数据源（支持分布式锁）
        if (lightCache.isDataGenerateMutex()) {
            Atom atom = AtomHolder.getAtom();
            if (atom != null) {
                String keyPath = "atom_" + key;
                Spot<Serializable, E> finalFastSpot = fastSpot;
                return atom.autoLock(keyPath, AtomLockType.NON_FAIR, -1, () -> {
                    // DCL双重检测，防止缓存击穿，多个请求同时进入数据中间件
                    Spot<Serializable, E> peekSpot = cache.get(key, new TypeReference<Serializable>() {}, eTypeRef);
                    if (peekSpot != null) {
                        E cacheData = peekSpot.getData();
                        finalFastSpot.setData(cacheData);
                        return cacheData;
                    }
                    E cacheData = dataGenerator.apply(id.toString());
                    // 如果数据中间件查询返回 null，那么在缓存中设置一个空缓存，防止缓存穿透（缓存(nil) -> 数据中间件(null)）
                    if (cacheData == null) {
                        cacheData = (E) ReflectUtil.newInstance(TypeUtil.type2Class(eTypeRef));
                    }
                    finalFastSpot.setData(cacheData);
                    cache.set(key, finalFastSpot);
                    return cacheData;
                });
            }
        }

        // 方案三：读取网络数据源（不支持分布式锁）
        data = dataGenerator.apply(fastSpot.getView().toString());
        // 如果返回值为null，不缓存
        if (data == null) {
            return data;
        }
        // 设置缓存数据
        fastSpot.setData(data);
        // 一级缓存（内存，默认缓存64个，超出时使用热点旧数据丢弃策略） -> 二级缓存（Redis）
        cache.set(key, fastSpot);
        return data;
    }

    /**
     * 擦除指定缓存数据
     * @param cache         缓存实例
     * @param id            标识值
     * @param keyGenerator  缓存key生成器
     */
    public static void erase(Cache cache, Serializable id, Function<Serializable, String> keyGenerator) {
        cache.erase(keyGenerator.apply(id));
    }

    /**
     * 更新缓存
     * @param cache             缓存实例
     * @param id                标识值
     * @param keyGenerator      缓存key产生器
     * @param dataGenerator     数据产生器
     * @param <E>               实体类型
     * @return                  缓存数据
     * @throws Throwable 更新异常
     */
    public static <E> E put(Cache cache, Serializable id,
                            Function<Serializable, String> keyGenerator, ThrowableFunction<String, E> dataGenerator) throws Throwable  {
        // 先执行数据操作
        String key = keyGenerator.apply(id);
        E data = dataGenerator.apply(key);

        // 返回值如果为空，清空缓存
        if (data == null) {
            erase(cache, id, keyGenerator);
            return data;
        }

        // 再存入缓存
        Spot<Serializable, E> fastSpot;
        LightCache lightCache = (LightCache) cache;
        if (lightCache.isEnableSuperCache() && !lightCache.isOnlyCacheL2()) {
            fastSpot = get(cache);
            if (fastSpot == null) {
                // 设置超级缓存
                set(cache, id);
                fastSpot = get(cache);
            }
        } else {
            fastSpot = new Spot<>();
            fastSpot.setView(id);
        }
        // 设置缓存数据
        fastSpot.setData(data);
        // 设置一级缓存 -> 二级缓存
        cache.set(key, fastSpot);
        return data;
    }
}
