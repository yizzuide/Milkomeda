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

import java.io.Serializable;

/**
 * Cache
 * 缓存接口
 *
 * V：标识数据
 * E：缓存业务数据
 *
 * @since 1.9.0
 * @version 1.17.0
 * @author yizzuide
 * <br>
 * Create at 2019/07/01 15:39
 */
public interface Cache {
    /**
     * 设置超级缓存
     *
     * @param id    缓存标识符
     */
    void set(Serializable id);

    /**
     * 获取超级缓存数据
     *
     * @param <E> 实体类型
     * @return  Spot
     */
    <E> Spot<Serializable, E> get();

    /**
     * 清除超级缓存
     */
    void remove();

    /**
     * 存入缓存
     * @param key       键
     * @param spot      缓存数据
     */
    void set(String key, Spot<Serializable, ?> spot);

    /**
     * 从缓存获取
     * <br>
     * 注意：这个简单方法只支持基本类型的包装类型以及String，其它类型请不要使用
     *
     * @param key   键
     * @param <E> 实体类型
     * @return      Spot
     * @deprecated  1.9.0
     */
    @Deprecated
    <E> Spot<Serializable, E> get(String key);

    /**
     * 从缓存获取
     *
     * @param key       键
     * @param vClazz    标识数据类型
     * @param eClazz    业务数据类型
     * @param <E> 实体类型
     * @return          Spot
     */
    <E> Spot<Serializable, E> get(String key, Class<Serializable> vClazz, Class<E> eClazz);

    /**
     * 从缓存获取
     *
     * @param key       键
     * @param vTypeRef  标识数据TypeReference
     * @param eTypeRef  业务数据TypeReference
     * @param <E> 实体类型
     * @return          Spot
     */
    <E> Spot<Serializable, E> get(String key, TypeReference<Serializable> vTypeRef, TypeReference<E> eTypeRef);

    /**
     * 根据key擦除指定缓存
     * @param key   缓存key
     */
    void erase(String key);

    /**
     * Check is cache in memory.
     * @param key cache key
     * @return ture if exists
     * @since 3.20.0
     */
    boolean isCacheL1Exists(String key);

    /**
     * Check is cache in redis.
     * @param key cache key
     * @return ture if exists
     * @since 3.14.0
     */
    boolean isCacheL2Exists(String key);
}
