package com.github.yizzuide.milkomeda.universe.algorithm.hash;

import java.util.function.Consumer;

/**
 * ConsistentHashFunc
 * 一致性Hash算法
 *
 * @author yizzuide
 * @since 3.8.0
 * Create at 2020/06/18 18:23
 */
public interface ConsistentHashFunc extends HashFunc {
    /**
     * 平衡hash因子
     * @return  hash因子
     */
    int balanceFactor();

    /**
     * 平衡hash生成算法
     * @param key       需要hash的key
     * @param generator 虚拟节点生成器
     */
    void balanceHash(Object key, Consumer<Long> generator);
}
