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
 * @version 3.12.10
 * <br>
 * Create at 2020/06/18 14:43
 */
public class ConsistentHashRing<T> {
    // 默认虚拟节点个数
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
     * 每添加一个实体节点，生成多个映射的虚拟节点
     * @param node 节点
     */
    public void add(T node) {
        // 如果是平衡一致性hash的实现
        if (hashFunc instanceof ConsistentHashFunc) {
            ConsistentHashFunc consistentHashFunc = (ConsistentHashFunc) this.hashFunc;
            int balanceFactor = consistentHashFunc.balanceFactor();
            int count = replicas / balanceFactor;
            for (int i = 0; i < count; i++) {
                consistentHashFunc.balanceHash(genVirtualKey(node, i), (hk) -> hashRing.put(hk, node));
            }
            return;
        }
        // 其它hash实现
        for (int i = 0; i < replicas; i++) {
            hashRing.put(hashFunc.hash(genVirtualKey(node, i)), node);
        }
    }

    /**
     * 移除一个节点的所有虚拟节点
     * @param node  节点
     */
    public void remove(T node) {
        // 如果是平衡一致性hash的实现
        if (hashFunc instanceof ConsistentHashFunc) {
            ConsistentHashFunc consistentHashFunc = (ConsistentHashFunc) this.hashFunc;
            int balanceFactor = consistentHashFunc.balanceFactor();
            int count = replicas / balanceFactor;
            for (int i = 0; i < count; i++) {
                consistentHashFunc.balanceHash(genVirtualKey(node, i), hashRing::remove);
            }
            return;
        }
        // 其它hash实现
        for (int i = 0; i < replicas; i++) {
            hashRing.remove(hashFunc.hash(genVirtualKey(node, i)));
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

    /**
     * 生成虚拟节点key
     * @param node  实体节点
     * @param seq   序号
     * @return  虚拟节点key
     * @since 3.12.10
     */
    private String genVirtualKey(Object node, int seq) {
        return node.toString() + V_NODE_SUFFIX + seq;
    }
}
