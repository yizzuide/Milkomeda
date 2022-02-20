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

import com.github.yizzuide.milkomeda.universe.algorithm.hash.CachedConsistentHashRing;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;

/**
 * ShardingFunction
 * 拆分函数对象
 *
 * @author yizzuide
 * @since 3.8.0
 * @version 3.9.0
 * Create at 2020/06/16 10:34
 */
@Slf4j
public class ShardingFunction {

    public static final String BEAN_ID = "ShardingFunction";

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
        return hash("fnv", key, nodeCount, replicas);
    }

    /**
     * Murmur算法一致性哈希实现（高性能，高不变流量）
     * @param key           拆分键
     * @param nodeCount     节点数
     * @param replicas      每个节点复制的虚拟节点数，推荐设置4的倍数
     * @return  目标节点
     */
    public long murmur(String key, long nodeCount, int replicas) {
        return hash("murmur", key, nodeCount, replicas);
    }

    /**
     * Ketama算法一致性哈希实现（与Murmur相当的高不变流量，高负载平衡性，推荐使用）
     * @param key           拆分键
     * @param nodeCount     节点数
     * @param replicas      每个节点复制的虚拟节点数，推荐设置4的倍数
     * @return  目标节点
     */
    public long ketama(String key, long nodeCount, int replicas) {
        return hash("ketama", key, nodeCount, replicas);
    }

    /**
     * 自定义算法一致性哈希实现
     * @param hashName      哈希算法名
     * @param key           拆分键
     * @param nodeCount     节点数
     * @param replicas      每个节点复制的虚拟节点数，推荐设置4的倍数
     * @return  目标节点
     * @since 3.12.10
     */
    public long hash(String hashName, String key, long nodeCount, int replicas) {
        return CachedConsistentHashRing.getInstance().lookForHashNode(key, nodeCount, replicas, hashName);
    }

    /**
     * 时间窗口滑动（可实现按创建日期拆分）
     * @param key   时间拆分键
     * @param startDate 开始时间（格式：yyyy-MM-dd）
     * @param daySlideWindow 滑动窗口天数（一个滑动窗口分一次）
     * @param expandWarnPercent 当前分表扩展警告占百分比，如：0.75
     * @return  拆分号
     * @since 3.9.0
     */
    public long rollDate(Date key, String startDate, long daySlideWindow, double expandWarnPercent) {
        long current = key.getTime();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDateTime startDateTime = LocalDateTime.parse(startDate, formatter);
        long start = startDateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
        // 计算当前累计天数
        long offset = current - start;
        if (offset < 0)  {
            throw new IllegalArgumentException("current time is less than the start time");
        }
        long days = Duration.ofMillis(offset).toDays();
        return roll(days, daySlideWindow, expandWarnPercent);
    }

    /**
     * 通用窗口滑动（可实现增长类型主键拆分）
     * @param key           拆分键
     * @param slideWindow   滑动窗口大小
     * @param expandWarnPercent 当前分表扩展警告占百分比，如：0.75
     * @return  拆分号
     * @since 3.9.0
     */
    public long roll(long key,  long slideWindow, double expandWarnPercent) {
        assert key >= 0 && slideWindow > 0;
        if (expandWarnPercent > 0) {
            // 获取系数
            BigDecimal factor = BigDecimal.valueOf(key).divide(BigDecimal.valueOf(slideWindow), 2, RoundingMode.DOWN);
            // 截取小数得当前分配百分比
            double percent = factor.subtract(new BigDecimal(factor.longValue())).doubleValue();
            if (percent > expandWarnPercent) {
                log.warn("Roll function of sundial sharding up to percent: {}", percent);
            }
        }
        if (key <= slideWindow || key % slideWindow == 0) {
            return key / slideWindow;
        }
        return BigDecimal.valueOf(key).divide(BigDecimal.valueOf(slideWindow), RoundingMode.UP).longValue() - 1;
    }
}
