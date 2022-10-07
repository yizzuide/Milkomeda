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

package com.github.yizzuide.milkomeda.light;

import java.io.Serializable;
import java.util.Map;

/**
 * Discard
 *
 * 缓存数据丢弃策略接口
 *
 * @since 1.8.0
 * @version 2.0.3
 * @author yizzuide
 * <br />
 * Create at 2019/06/28 14:50
 */
public interface Discard {

    /**
     * 返回具体的缓存数据类
     *
     * @return 缓存数据类
     */
    Class<? extends SortSpot> spotClazz();

    /**
     * 转型
     *
     * @param key       缓存key
     * @param spot      缓存数据
     * @param expire    内存缓存过期时间（单位：s)
     * @return  Spot
     */
    Spot<Serializable, Object> deform(String key, Spot<Serializable, Object> spot, long expire);

    /**
     * 提升缓存数据的权重
     *
     * @param spot  缓存数据
     * @return 是否放弃缓存机会（放弃会马上删除缓存）
     */
    boolean ascend(Spot<Serializable, Object> spot);

    /**
     * 丢弃缓存数据
     * @param cacheMap          缓存容器
     * @param l1DiscardPercent  丢弃百分数
     */
    void discard(Map<String, Spot<Serializable, Object>> cacheMap, float l1DiscardPercent);
}
