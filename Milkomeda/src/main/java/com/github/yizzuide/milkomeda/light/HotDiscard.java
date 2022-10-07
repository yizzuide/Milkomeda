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
 * HotDiscard
 *
 * 低频热点丢弃方案
 *
 * @since 1.8.0
 * @version 2.0.3
 * @author yizzuide
 * <br />
 * Create at 2019/06/28 14:58
 */
public class HotDiscard extends SortDiscard {

    @Override
    public Class<? extends SortSpot> spotClazz() {
        return HotSpot.class;
    }

    @Override
    public Spot<Serializable, Object> deform(String key, Spot<Serializable, Object> spot, long expire) {
        HotSpot<Serializable, Object> hotSpot = (HotSpot<Serializable, Object>) super.deform(key, spot, expire);
        if (null == hotSpot.getStar()) {
            hotSpot.setStar(0L);
        }
        return hotSpot;
    }

    @Override
    public boolean ascend(Spot<Serializable, Object> spot) {
        HotSpot<Serializable, Object> hotSpot = (HotSpot<Serializable, Object>) spot;
        hotSpot.setStar(hotSpot.getStar() + 1);
        return false;
    }

    @Override
    protected Comparator<? extends SortSpot<Serializable, Object>> comparator() {
        return Comparator.<HotSpot<Serializable, Object>, Long>comparing(HotSpot::getStar);
    }
}
