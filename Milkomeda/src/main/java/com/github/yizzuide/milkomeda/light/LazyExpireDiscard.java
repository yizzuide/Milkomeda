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
