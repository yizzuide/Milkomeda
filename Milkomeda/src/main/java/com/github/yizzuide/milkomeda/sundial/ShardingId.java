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

package com.github.yizzuide.milkomeda.sundial;

/**
 * ShardingId
 * 1bit + 41bit时间差 + 3bit机器号 + 7bit业务号 + 4bit区分号 + 8bit自增序列
 *
 * @author yizzuide
 * @since 3.8.0
 * Create at 2020/06/22 09:53
 */
public class ShardingId {
    // 开始时间 2020-01-01
    private static final long EPOCH = 1577808000000L;
    // 时间位（可用69年，也就是2089-01-01）
    public static final long TIME_BITS = 41L;
    // 机器位（集群8台，满足一定量的并发需求）
    private final static long WORKER_ID_BITS = 3L;
    // 业务位（128张业务表）
    private final static long BUSINESS_ID_BITS = 7L;
    // 拆分位（分16张表能记录3亿多条记录，满足绝大多需求）
    final static long SHARD_BITS = 4L;
    // 序列号位
    static final long SN_BITS = 8L;
    // 最大序列号
    private static final long MAX_SN = ~(-1L << SN_BITS);
    // 最大机器数
    private final static long MAX_WORKER_ID = ~(-1L << WORKER_ID_BITS);
    // 最大业务数
    private final static long MAX_BUSINESS_ID = ~(-1L << BUSINESS_ID_BITS);
    //  最大拆分数
    private final static long MAX_SHARED_ID = ~(-1L << SHARD_BITS);
    //  序列号开始
    private static long seqStart = 0L;
    // 上次时间
    private static long preTime = -1L;

    /**
     * 获取序列号
     * @param workerId      机器id，当前业务布署的机器序号，范围[0, 8)
     * @param businessId    业务id，范围[0, 128)
     * @param sharding      拆分序号，范围[0, 16)
     * @return 唯一序列号
     */
    public static long nextId(long workerId, long businessId, long sharding) {
        if (workerId > MAX_WORKER_ID || workerId < 0) {
            throw new IllegalArgumentException(String.format("workerId:%d invalid, range is 0 to %d", workerId, MAX_WORKER_ID));
        }
        if (businessId > MAX_BUSINESS_ID || businessId < 0) {
            throw new IllegalArgumentException(String.format("businessId:%d invalid, range is 0 to %d", sharding, MAX_BUSINESS_ID));
        }
        if (sharding > MAX_SHARED_ID || sharding < 0) {
            throw new IllegalArgumentException(String.format("sharding:%d invalid, range is 0 to %d", sharding, MAX_SHARED_ID));
        }
        synchronized (ShardingId.class) {
            long timestamp = currentTime();
            // 时钟回退，抛出异常
            if (timestamp < preTime) {
                throw new RuntimeException(String.format("clock moved backwards, refusing to generate id for %d milliseconds", preTime - timestamp));
            }
            if (preTime == timestamp) {
                seqStart = (seqStart + 1) & MAX_SN;
                // 如果序列号处于起点，获取比上次更新的时间
                if (seqStart == 0) {
                    timestamp = nextTime(preTime);
                }
            } else {
                seqStart = 0L;
            }
            preTime = timestamp;
            return (timestamp - EPOCH) << (SN_BITS + SHARD_BITS + BUSINESS_ID_BITS + WORKER_ID_BITS)
                    | workerId << (SN_BITS + SHARD_BITS + BUSINESS_ID_BITS)
                    | businessId << (SN_BITS + SHARD_BITS)
                    | sharding << SN_BITS
                    | seqStart;
        }
    }

    private static long currentTime() {
        return System.currentTimeMillis();
    }

    private static long nextTime(long lastTimestamp) {
        long timestamp = currentTime();
        while (timestamp <= lastTimestamp) {
            timestamp = currentTime();
        }
        return timestamp;
    }
}
