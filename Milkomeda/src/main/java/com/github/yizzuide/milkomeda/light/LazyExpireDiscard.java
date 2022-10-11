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
import java.util.Comparator;

/**
 * LazyExpireDiscard
 * 懒惰过期丢弃方案
 *
 * @author yizzuide
 * @since 2.0.1
 * @version 2.0.3
 * <br>
 * Create at 2019/12/23 16:09
 */
public class LazyExpireDiscard extends SortDiscard {

    @Override
    public Class<? extends SortSpot> spotClazz() {
        return LazyExpireSpot.class;
    }

    @Override
    public Spot<Serializable, Object> deform(String key, Spot<Serializable, Object> spot, long expire) {
        LazyExpireSpot<Serializable, Object> lazyExpireSpot = (LazyExpireSpot<Serializable, Object>) super.deform(key, spot, expire);
        // 设置过期时间
        if (null == lazyExpireSpot.getExpireTime()) {
            if (expire > 0) {
                lazyExpireSpot.setExpireTime(System.currentTimeMillis() + expire * 1000);
            }
        }
        return lazyExpireSpot;
    }

    @Override
    public boolean ascend(Spot<Serializable, Object> spot) {
        LazyExpireSpot<Serializable, Object> lazyExpireSpot = (LazyExpireSpot<Serializable, Object>) spot;
        if (lazyExpireSpot.getExpireTime() == null) {
            return false;
        }
        long currentTime = System.currentTimeMillis();
        // 缓存过期，放弃缓存机会
        return currentTime > lazyExpireSpot.getExpireTime();
    }

    @Override
    protected Comparator<? extends SortSpot<Serializable, Object>> comparator() {
        // 不支持排行丢弃
        return null;
    }
}
