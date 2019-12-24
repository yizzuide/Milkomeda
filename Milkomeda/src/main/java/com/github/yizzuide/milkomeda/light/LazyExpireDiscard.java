package com.github.yizzuide.milkomeda.light;

import com.github.yizzuide.milkomeda.universe.context.ApplicationContextHolder;

import java.io.Serializable;
import java.util.Comparator;

/**
 * LazyExpireDiscard
 * 懒惰过期丢弃方案
 *
 * @author yizzuide
 * @since 2.0.1
 * Create at 2019/12/23 16:09
 */
public class LazyExpireDiscard extends SortDiscard {

    @Override
    public Class<? extends SortSpot> spotClazz() {
        return LazyExpireSpot.class;
    }

    @Override
    public Spot<Serializable, Object> deform(String key, Spot<Serializable, Object> spot) {
        LazyExpireSpot<Serializable, Object> lazyExpireSpot = (LazyExpireSpot<Serializable, Object>) super.deform(key, spot);
        // 设置一级缓存过期
        if (null == lazyExpireSpot.getExpireTime()) {
            long l1Expire = ApplicationContextHolder.get().getBean(LightProperties.class).getL1Expire();
            if (l1Expire > 0) {
                lazyExpireSpot.setExpireTime(System.currentTimeMillis() + l1Expire * 1000);
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
