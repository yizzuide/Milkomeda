package com.github.yizzuide.milkomeda.universe.algorithm.hash;

/**
 * HashFunc
 * Hash算法
 *
 * @author yizzuide
 * @since 3.8.0
 * Create at 2020/06/18 14:44
 */
public interface HashFunc {
    /**
     * 生成hash
     * @param key   生成hash的键
     * @return  hash后的值
     */
    long hash(Object key);
}
