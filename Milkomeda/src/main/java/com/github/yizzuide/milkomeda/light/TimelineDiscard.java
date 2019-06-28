package com.github.yizzuide.milkomeda.light;

import java.util.*;

/**
 * TimelineDiscard
 *
 * 时间线丢弃方案
 *
 * @since 1.8.0
 * @author yizzuide
 * Create at 2019/06/28 16:25
 */
public class TimelineDiscard<V, E> extends SortDiscard<V, E> {

    @Override
    public Class<? extends SortSpot> spotClazz() {
        return TimelineSpot.class;
    }

    @Override
    public Spot<V, E> deform(String key, Spot<V, E> spot) {
        TimelineSpot<V, E> timelineSpot = (TimelineSpot<V, E>) super.deform(key, spot);
        if (null == timelineSpot.getTime()) {
            timelineSpot.setTime(new Date());
        }
        return timelineSpot;
    }

    @Override
    public void ascend(Spot<V, E> spot) {
        TimelineSpot<V, E> timelineSpot = (TimelineSpot<V, E>) spot;
        timelineSpot.setTime(new Date());
    }

    @Override
    protected Comparator<? extends SortSpot<V, E>> comparator() {
        return Comparator.<TimelineSpot<V, E>, Date>comparing(TimelineSpot::getTime);
    }
}
