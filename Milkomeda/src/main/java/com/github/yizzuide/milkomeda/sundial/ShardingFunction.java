package com.github.yizzuide.milkomeda.sundial;

import com.github.yizzuide.milkomeda.universe.algorithm.hash.HashFunc;
import com.github.yizzuide.milkomeda.universe.algorithm.hash.ConsistentHashRing;
import com.github.yizzuide.milkomeda.universe.algorithm.hash.FNV132Hash;
import com.github.yizzuide.milkomeda.universe.algorithm.hash.KetamaHash;
import com.github.yizzuide.milkomeda.universe.algorithm.hash.MurmurHash;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * ShardingFunction
 * 拆分函数对象
 *
 * @author yizzuide
 * @since 3.8.0
 * Create at 2020/06/16 10:34
 */
public class ShardingFunction {

    public static final String BEAN_ID = "ShardingFunction";

    private final Map<String, ConsistentHashRing<Long>> cachedConsistentHashMap = new ConcurrentHashMap<>();

    /**
     * 格式化函数
     * @param format    格式字符串
     * @param args      参数
     * @return  结果字符串
     */
    public String format(String format, Object... args) {
        return String.format(format, args);
    }

    /**
     * 取模函数
     * @param key       拆分键
     * @param count     节点个数
     * @return  取模结果
     */
    public long mod(long key, long count) {
        return key % count;
    }

    /**
     * 取ShardingId生成的拆分号
     * @param key   拆分键
     * @return  拆分号
     */
    public long id(long key) {
        return seq(key, ShardingId.SN_BITS, ShardingId.SHARD_BITS);
    }

    /**
     * 自定义序列号抽取函数
     * @param key       拆分键
     * @param bitStart  二进制开始位（从0开始，且从右边开始计数）
     * @param bitCount  二进制长度（从右边向左取位数）
     * @return 取值结果
     */
    public long seq(long key, long bitStart, long bitCount) {
        long maxSize = ~(-1L << bitCount);
        return (key >> bitStart) & maxSize;
    }

    /**
     * FNV132算法一致性哈希函数实现（与Murmur相当的性能和不变流量）
     * @param key           拆分键
     * @param nodeCount     节点数
     * @param replicas      每个节点复制的虚拟节点数，推荐设置4的倍数
     * @return  目标节点
     */
    public long fnv(String key, long nodeCount, int replicas) {
        return hash(key, nodeCount, replicas, new FNV132Hash());
    }

    /**
     * Murmur算法一致性哈希实现（高性能，高不变流量）
     * @param key           拆分键
     * @param nodeCount     节点数
     * @param replicas      每个节点复制的虚拟节点数，推荐设置4的倍数
     * @return  目标节点
     */
    public long murmur(String key, long nodeCount, int replicas) {
        return hash(key, nodeCount, replicas, new MurmurHash());
    }

    /**
     * Ketama算法一致性哈希实现（与Murmur相当的高不变流量，高负载平衡性，推荐使用）
     * @param key           拆分键
     * @param nodeCount     节点数
     * @param replicas      每个节点复制的虚拟节点数，推荐设置4的倍数
     * @return  目标节点
     */
    public long ketama(String key, long nodeCount, int replicas) {
        return hash(key, nodeCount, replicas, new KetamaHash());
    }

    private long hash(String key, long nodeCount, int replicas, HashFunc hashFunc) {
        String cacheKey = nodeCount + "_" + replicas;
        // 缓存哈希环
        ConsistentHashRing<Long> consistentHashRing = cachedConsistentHashMap.get(cacheKey);
        if (consistentHashRing == null) {
            // 生成节点
            List<Long> nodes = new ArrayList<>();
            for (long i = 0; i < nodeCount; i++) {
                nodes.add(i);
            }
            // 创建哈希环
            consistentHashRing = new ConsistentHashRing<>(hashFunc, replicas, nodes);
            // 添加到缓存
            cachedConsistentHashMap.put(cacheKey, consistentHashRing);
        }
        return consistentHashRing.get(key);
    }
}
