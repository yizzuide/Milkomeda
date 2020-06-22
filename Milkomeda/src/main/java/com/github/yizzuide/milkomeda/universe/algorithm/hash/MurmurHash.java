package com.github.yizzuide.milkomeda.universe.algorithm.hash;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * Murmur：高性能hash算法，高不变流量，低平衡性
 *
 * @author yizzuide
 * @since 3.8.0
 * Create at 2020/06/18 17:15
 */
public class MurmurHash implements HashFunc {
    @Override
    public long hash(Object key) {
        ByteBuffer buf = ByteBuffer.wrap(key.toString().getBytes());
        int seed = 0x1234ABCD;
        ByteOrder byteOrder = buf.order();
        buf.order(ByteOrder.LITTLE_ENDIAN);
        long m = 0xc6a4a7935bd1e995L;
        int r = 47;
        long h = seed ^ (buf.remaining() * m);
        long k;
        while (buf.remaining() >= 8) {
            k = buf.getLong();
            k *= m;
            k ^= k >>> r;
            k *= m;
            h ^= k;
            h *= m;
        }
        if (buf.remaining() > 0) {
            ByteBuffer finish = ByteBuffer.allocate(8).order(ByteOrder.LITTLE_ENDIAN);
            // for big-endian version, do this first:
            // finish.position(8-buf.remaining());
            finish.put(buf).rewind();
            h ^= finish.getLong();
            h *= m;
        }
        h ^= h >>> r;
        h *= m;
        h ^= h >>> r;
        buf.order(byteOrder);
        return h & 0xffffffffL;
    }
}
