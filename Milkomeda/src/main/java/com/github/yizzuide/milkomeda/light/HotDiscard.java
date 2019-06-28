package com.github.yizzuide.milkomeda.light;


import java.util.Comparator;

/**
 * HotDiscard
 *
 * 低频热点丢弃方案
 *
 * @since 1.8.0
 * @author yizzuide
 * Create at 2019/06/28 14:58
 */
public class HotDiscard<V, E> extends SortDiscard<V, E> {

    @Override
    public Class<? extends SortSpot> spotClazz() {
        return HotSpot.class;
    }

    @Override
    public Spot<V, E> deform(String key, Spot<V, E> spot) {
        HotSpot<V, E> hotSpot = (HotSpot<V, E>) super.deform(key, spot);
        if (null == hotSpot.getStar()) {
            hotSpot.setStar(0L);
        }
        return hotSpot;
    }

    @Override
    public void ascend(Spot<V, E> spot) {
        HotSpot<V, E> hotSpot = (HotSpot<V, E>) spot;
        hotSpot.setStar(hotSpot.getStar() + 1);
    }

    @Override
    protected Comparator<? extends SortSpot<V, E>> comparator() {
        return Comparator.<HotSpot<V, E>, Long>comparing(HotSpot::getStar);
    }
}
