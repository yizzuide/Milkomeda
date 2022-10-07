/*
 * Copyright (c) 2022 yizzuide All rights Reserved.
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

package com.github.yizzuide.milkomeda.universe.algorithm.hash;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.*;

/**
 * CachedConsistentHashRing
 * 可缓存的哈希环
 *
 * @author yizzuide
 * @since 3.12.10
 * <br />
 * Create at 2022/02/20 14:47
 */
@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CachedConsistentHashRing {
    // 哈希算法类
    private static final Map<String, Class<? extends HashFunc>> hashFuncClassMap = new HashMap<>();
    // 哈希算法实例
    private final Map<String, HashFunc> hashFuncMap = new HashMap<>();
    // 一致性Hash环
    private final Map<String, ConsistentHashRing<Long>> container = new HashMap<>();

    private static class CachedConsistentHashRingHolder {
        static CachedConsistentHashRing cachedConsistentHashRing = new CachedConsistentHashRing();
    }

    public static CachedConsistentHashRing getInstance() {
        return CachedConsistentHashRingHolder.cachedConsistentHashRing;
    }

    static {
        hashFuncClassMap.put("fnv", FNV132Hash.class);
        hashFuncClassMap.put("murmur", MurmurHash.class);
        hashFuncClassMap.put("ketama", KetamaHash.class);
    }

    /**
     * 查找命中的哈希节点
     * @param key           拆分键
     * @param nodeCount     节点数
     * @param replicas      每个节点复制的虚拟节点数
     * @param hashName      哈希算法名，已注册的有：fnv, murmur, ketama
     * @return  哈希节点
     */
    public long lookForHashNode(String key, long nodeCount, int replicas, String hashName) {
        String cacheKey = hashName + "@" + nodeCount + "_" + replicas;
        // 缓存哈希环
        ConsistentHashRing<Long> consistentHashRing = container.get(cacheKey);
        if (consistentHashRing == null) {
            // 防止多线程重复创建
            synchronized (this) {
                //noinspection ConstantConditions: for double check!
                if (consistentHashRing == null) {
                    // 生成节点
                    List<Long> nodes = new ArrayList<>();
                    for (long i = 0; i < nodeCount; i++) {
                        nodes.add(i);
                    }
                    // 创建哈希环
                    consistentHashRing = new ConsistentHashRing<>(this.get(hashName), replicas, nodes);
                    // 添加到缓存
                    container.put(cacheKey, consistentHashRing);
                }
            }
        }
        return consistentHashRing.get(key);
    }

    /**
     * 注册hash算法实现
     * @param hashName  hash算法
     * @param hashFuncClass  hash实现类
     * @since 3.13.0
     */
    public static void register(String hashName, Class<? extends HashFunc> hashFuncClass) {
        hashFuncClassMap.put(hashName, hashFuncClass);
    }

    /**
     * 根据算法名，获取hash算法实例
     * @param hashName  hash算法
     * @return  hash算法实例
     * @since 3.13.0
     */
    private HashFunc get(String hashName) {
        return Optional.ofNullable(hashFuncMap.get(hashName)).orElseGet(() -> {
            HashFunc hashFunc = null;
            Class<? extends HashFunc> hashFuncClass = hashFuncClassMap.get(hashName);
            try {
                hashFunc = hashFuncClass.newInstance();
                hashFuncMap.put(hashName, hashFunc);
            } catch (Exception e) {
                log.error("Create hash Function object error with msg: {}", e.getMessage(), e);
            }
            return hashFunc;
        });
    }
}
