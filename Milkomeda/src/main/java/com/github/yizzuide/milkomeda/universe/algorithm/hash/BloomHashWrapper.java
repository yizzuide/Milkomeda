package com.github.yizzuide.milkomeda.universe.algorithm.hash;

/**
 * BloomHashWrapper
 * 布隆过滤器特点：
 * 1. 如果该元素被判断不存在，那么一定不存在（该特性可用于防缓存穿透（击穿）问题）
 * 2. 如果该元素被判断存在，那么很可能存在
 * @author yizzuide
 * @since 3.9.0
 * Create at 2020/06/23 15:17
 */
public class BloomHashWrapper<T> {
    /**
     * hash函数应用次数（次数越多，bit位扩散填充得越满，假阳性越多）
     */
    private final int hashFuncCount;

    /**
     * bit位数
     */
    private final int bitSize;

    /**
     * Hash函数
     */
    private final HashFunc hashFunc;

    /**
     * 使用Murmur Hash
     * @param expectedInsertions 期望的插入总量
     */
    public BloomHashWrapper(int expectedInsertions) {
        // 0.03借鉴自Guava布隆器默认误判率
        this(new MurmurHash(), expectedInsertions, 0.03);
    }

    /**
     * 自定义误判率构造
     * @param expectedInsertions    期望的插入总量
     * @param fpp                   误判率
     */
    public BloomHashWrapper(int expectedInsertions, double fpp) {
        this(new MurmurHash(), expectedInsertions, fpp);
    }

    /**
     * 自定义Hash与误判率构造
     * @param hashFunc              Hash函数实现
     * @param expectedInsertions    期望的插入总量
     * @param fpp                   误判率
     */
    public BloomHashWrapper(HashFunc hashFunc, int expectedInsertions, double fpp) {
        bitSize = getBits(expectedInsertions, fpp);
        hashFuncCount = getHashFuncNum(expectedInsertions, bitSize);
        this.hashFunc = hashFunc;
    }

    /**
     * 根据值，获取bit偏移位
     * @param value 值
     * @return  bit偏移位
     */
    public int[] offset(T value) {
        int[] offset = new int[hashFuncCount];
        long hash64 = hashFunc.rawHash(value);
        int hash1 = (int) hash64;
        int hash2 = (int) (hash64 >>> 32);
        for (int i = 1; i <= hashFuncCount; i++) {
            int nextHash = hash1 + i * hash2;
            if (nextHash < 0) {
                nextHash = ~nextHash;
            }
            offset[i - 1] = nextHash % bitSize;
        }
        return offset;
    }

    private int getBits(long insertions, double fpp) {
        if (fpp == 0) {
            fpp = Double.MIN_VALUE;
        }
        return (int) (-insertions * Math.log(fpp) / (Math.log(2) * Math.log(2)));
    }

    private int getHashFuncNum(int insertions, int bitSize) {
        return Math.max(1, (int) Math.round((double) bitSize / insertions * Math.log(2)));
    }
}
