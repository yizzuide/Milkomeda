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

package com.github.yizzuide.milkomeda.particle;

import com.github.yizzuide.milkomeda.universe.algorithm.hash.BloomHashWrapper;
import com.github.yizzuide.milkomeda.util.RedisUtil;
import org.springframework.data.redis.connection.RedisConnection;
import com.github.yizzuide.milkomeda.util.Strings;

import java.util.Collections;
import java.util.List;

/**
 * BloomLimiter
 * 布隆限制器
 *
 * @author yizzuide
 * @since 3.9.0
 * @version 3.12.10
 * Create at 2020/06/23 16:03
 */
public class BloomLimiter extends LimitHandler {
    /**
     * 配置存储bitmap的key
     */
    private String bitKey;
    /**
     * 键值分隔器（limit的key必需包含过滤的值，且通过该分隔器能获取）
     */
    @SuppressWarnings({"FieldCanBeLocal", "FieldMayBeFinal"})
    private String valueSeparator = "_";

    /**
     * 记录数据量
     */
    @SuppressWarnings({"FieldCanBeLocal", "FieldMayBeFinal"})
    private int insertions = 2 << 24; // 33554432 ~= 12M

    /**
     * 误差率
     */
    @SuppressWarnings({"FieldCanBeLocal", "FieldMayBeFinal"})
    private double fpp = 0.03;

    /**
     * Bloom Hash操作类
     */
    private volatile BloomHashWrapper<String> bloomHashWrapper;

    @Override
    public <R> R limit(String key, Process<R> process) throws Throwable {
        int splitIndex = key.lastIndexOf(valueSeparator);
        // 如果没有设置bitKey，自动设置redis的key前辍
        if (Strings.isEmpty(bitKey)) {
            bitKey = key.substring(0, splitIndex);
        }
        String value = key.substring(splitIndex + 1);
        int[] offset = getBloomHashWrapper().offset(value);
        Particle particle = null;
        for (int i : offset) {
            Boolean hit = getRedisTemplate().execute((RedisConnection connection) -> connection.getBit(bitKey.getBytes(), i));
            assert hit != null;
            // 只要有一bit为0，即匹配失败
            if (!hit) {
                particle = new Particle(this.getClass(), true, 0);
                break;
            }
        }
        if (particle == null) {
            particle = new Particle(this.getClass(), false, 1);
        }
        return next(particle, key, process);
    }

    /**
     * 添加数据到bitmap
     * @param value 记录值
     */
    public void add(String value) {
        addAll(Collections.singletonList(value));
    }

    /**
     * 添加数据到bitmap
     * @param values 记录列表
     */
    public void addAll(List<String> values) {
        assert bitKey != null;
        RedisUtil.batchConn((connection) -> {
            for (String value : values) {
                int[] offset = getBloomHashWrapper().offset(value);
                for (int i : offset) {
                    connection.setBit(bitKey.getBytes(), i, true);
                }
            }
        }, getRedisTemplate());
    }

    private BloomHashWrapper<String> getBloomHashWrapper() {
        // 双重检测
        if (bloomHashWrapper == null) {
            synchronized (this) {
                if (bloomHashWrapper == null) {
                    bloomHashWrapper = new BloomHashWrapper<>(insertions, fpp);
                }
            }
        }
        return bloomHashWrapper;
    }
}
