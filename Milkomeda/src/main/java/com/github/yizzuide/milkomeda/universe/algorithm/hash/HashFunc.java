package com.github.yizzuide.milkomeda.universe.algorithm.hash;

/**
 * HashFunc
 * Hash算法
 *
 * @author yizzuide
 * @since 3.8.0
 * @version 3.9.0
 * Create at 2020/06/18 14:44
 */
public interface HashFunc {
    /**
     * 修正hash
     * @param key   生成hash的键
     * @return  hash值
     */
    long hash(Object key);

    /**
     * 原始hash
     * @param key   生成hash的键
     * @return  hash值
     */
    long rawHash(Object key);
}
