package com.github.yizzuide.milkomeda.universe.algorithm.hash;

import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;

/**
 * ConsistentHash
 * 一致性哈希环
 *
 * @author yizzuide
 * @since 3.8.0
 * Create at 2020/06/18 14:43
 */
public class ConsistentHashRing<T> {
    // 虚拟节点个数
    private final static int V_NODE_SIZE = 4;
    // 虚拟节点后缀
    private final static String V_NODE_SUFFIX = "#";

    /**
     * Hash算法
     */
    private final HashFunc hashFunc;

    /**
     * 复制虚拟节点数
     */
    private final int replicas;

    /**
     * 哈希环实现
     */
    private final TreeMap<Long, T> hashRing = new TreeMap<>();

    public ConsistentHashRing(int replicas, Collection<T> nodes) {
        this(new KetamaHash(), replicas, nodes);
    }

    public ConsistentHashRing(HashFunc hashFunc, int replicas, Collection<T> nodes) {
        this.replicas = Math.max(replicas, V_NODE_SIZE);
        this.hashFunc = hashFunc;
        for (T node : nodes) {
            add(node);
        }
    }

    /**
     * 根据设置的虚拟节点来拷贝
     * @param node 节点
     */
    public void add(T node) {
        if (hashFunc instanceof ConsistentHashFunc) {
            ConsistentHashFunc consistentHashFunc = (ConsistentHashFunc) this.hashFunc;
            int balanceFactor = consistentHashFunc.balanceFactor();
            int count = V_NODE_SIZE / balanceFactor;
            for (int i = 0; i < count; i++) {
                consistentHashFunc.balanceHash(node.toString() + V_NODE_SUFFIX + i, (hk) -> hashRing.put(hk, node));
            }
            return;
        }
        for (int i = 0; i < replicas; i++) {
            hashRing.put(hashFunc.hash(node.toString() + V_NODE_SUFFIX + i), node);
        }
    }

    /**
     * 移除一点节点的所有虚拟节点
     * @param node  节点
     */
    public void remove(T node) {
        if (hashFunc instanceof ConsistentHashFunc) {
            ConsistentHashFunc consistentHashFunc = (ConsistentHashFunc) this.hashFunc;
            int balanceFactor = consistentHashFunc.balanceFactor();
            int count = V_NODE_SIZE / balanceFactor;
            for (int i = 0; i < count; i++) {
                consistentHashFunc.balanceHash(node.toString() + V_NODE_SUFFIX + i, hashRing::remove);
            }
            return;
        }
        for (int i = 0; i < replicas; i++) {
            hashRing.remove(hashFunc.hash(node.toString() + V_NODE_SUFFIX + i));
        }
    }

    /**
     * 从Hash环上查找匹配的下一个节点
     * @param key   需要hash的key
     * @return  节点
     */
    public T get(Object key) {
        if (hashRing.isEmpty()) {
            return null;
        }
        long hash = hashFunc.hash(key);
        // 获取环上等于当前hash或比它的值大的最近的一个虚拟节点
        Map.Entry<Long, T> locateEntry = hashRing.ceilingEntry(hash);
        // 超过尾部则取第一个节点
        if (locateEntry == null) {
            return hashRing.firstEntry().getValue();
        }
        return locateEntry.getValue();
    }
}
