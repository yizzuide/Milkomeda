package com.github.yizzuide.milkomeda.light;


import java.io.Serializable;
import java.util.Comparator;

/**
 * HotDiscard
 *
 * 低频热点丢弃方案
 *
 * @since 1.8.0
 * @version 1.17.0
 * @author yizzuide
 * Create at 2019/06/28 14:58
 */
public class HotDiscard extends SortDiscard {

    @Override
    public Class<? extends SortSpot> spotClazz() {
        return HotSpot.class;
    }

    @Override
    public Spot<Serializable, Object> deform(String key, Spot<Serializable, Object> spot) {
        HotSpot<Serializable, Object> hotSpot = (HotSpot<Serializable, Object>) super.deform(key, spot);
        if (null == hotSpot.getStar()) {
            hotSpot.setStar(0L);
        }
        return hotSpot;
    }

    @Override
    public void ascend(Spot<Serializable, Object> spot) {
        HotSpot<Serializable, Object> hotSpot = (HotSpot<Serializable, Object>) spot;
        hotSpot.setStar(hotSpot.getStar() + 1);
    }

    @Override
    protected Comparator<? extends SortSpot<Serializable, Object>> comparator() {
        return Comparator.<HotSpot<Serializable, Object>, Long>comparing(HotSpot::getStar);
    }
}
