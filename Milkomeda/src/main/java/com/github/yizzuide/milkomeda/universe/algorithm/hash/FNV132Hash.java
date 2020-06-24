package com.github.yizzuide.milkomeda.universe.algorithm.hash;

/**
 * FNV132hash: FNV 能快速 hash 大量数据并保持较小的冲突率，它的高度分散使它适用于 hash 一些非常相近的字符串
 *
 * @author yizzuide
 * @since 3.8.0
 * Create at 2020/06/18 14:46
 */
public class FNV132Hash implements HashFunc {
    private static final long FNV_32_INIT = 2166136261L;
    private static final int FNV_32_PRIME = 16777619;

    @Override
    public long hash(Object key) {
        return Math.abs(rawHash(key));
    }

    @Override
    public long rawHash(Object key) {
        String data = key.toString();
        int hash = (int)FNV_32_INIT;
        for (int i = 0; i < data.length(); i++)
            hash = (hash ^ data.charAt(i)) * FNV_32_PRIME;
        hash += hash << 13;
        hash ^= hash >> 7;
        hash += hash << 3;
        hash ^= hash >> 17;
        hash += hash << 5;
        return hash;
    }
}
