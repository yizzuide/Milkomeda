package com.github.yizzuide.milkomeda.light;

import com.github.yizzuide.milkomeda.pulsar.PulsarHolder;
import com.github.yizzuide.milkomeda.util.JSONUtil;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.Map;
import java.util.concurrent.ConcurrentSkipListMap;

/**
 * LightCache
 *
 * 缓存方式： 一级缓存（内存，缓存个数可控）| 二级缓存（Redis）
 *
 * V：缓存视图
 * E：缓存业务数据
 *
 * @since 1.8.0
 * @author yizzuide
 * Create at 2019/06/28 13:33
 */
public class LightCache<V, E> {
    /**
     * 默认一级缓存最大个数
     */
    private static final Integer DEFAULT_L1_MAX_COUNT = 64;

    /**
     * 默认一级缓存一次性移除百分比
     */
    private static final Float DEFAULT_L1_DISCARD_PERCENT = 0.1F;

    /**
     * 一级缓存最大个数
     */
    @Setter
    @Getter
    private Integer l1MaxCount = DEFAULT_L1_MAX_COUNT;

    /**
     * 一级缓存一次性移除百分比
     */
    @Getter
    private Float l1DiscardPercent = DEFAULT_L1_DISCARD_PERCENT;

    /**
     * 只写入一级缓存, 默认为false
     */
    @Setter
    @Getter
    private Boolean onlyCacheL1 = false;

    /**
     * 丢弃策略，默认根据热点
     */
    @Setter
    @Getter
    private Discard<V, E> discardStrategy = new HotDiscard<>();

    /**
     * 一级缓存容器
     */
    private Map<String, Spot<V, E>> cacheMap = new ConcurrentSkipListMap<>();

    /**
     * 二级缓存容器
     */
    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    /**
     * 首次存入缓存
     * @param key       键
     * @param spot      缓存数据
     */
    public void set(String key, Spot<V, E> spot) {
        // 类型转型（会触发初始化排序状态字段）
        spot = discardStrategy.deform(key, spot);

        // 开始缓存
        cache(key, spot);
    }

    /**
     * 执行缓存
     * @param key   键
     * @param spot  缓存数据
     */
    private void cache(String key, Spot<V, E> spot) {
        // 一级缓存
        cacheL1(key, spot);

        // 二级缓存
        if (!onlyCacheL1) {
            cacheL2(key, spot);
        }
    }

    /**
     * 二级缓存
     * @param key   键
     * @param spot  缓存数据
     */
    private void cacheL2(String key, Spot<V, E> spot) {
        // 异步添加到Redis（由于序列化稍微耗时）
        PulsarHolder.getPulsar().post(() -> {
            String json = JSONUtil.serialize(spot);
            stringRedisTemplate.opsForValue().set(key, json);
        });
    }

    /**
     * 一级缓存
     * @param key   键
     * @param spot  缓存数据
     */
    private void cacheL1(String key, Spot<V, E> spot) {
        // 一级缓存超出最大个数
        if ((cacheMap.size() + 1) > l1MaxCount) {
            // 根据选择的策略来丢弃数据
            discardStrategy.discard(cacheMap, l1DiscardPercent);
        }

        // 提升当前缓存数据权重
        discardStrategy.ascend(spot);

        // 添加到一级缓存
        cacheMap.put(key, spot);
    }

    /**
     * 从缓存获取
     * @param key   键
     * @return      Spot
     */
    @SuppressWarnings("unchecked")
    public Spot<V, E> get(String key) {
        Spot<V, E> spot = cacheMap.get(key);
        if (null != spot) {
            // 提升当前缓存数据权重
            discardStrategy.ascend(spot);
            return spot;
        }

        // 从二级缓存中查找
        if (!onlyCacheL1) {
            String json = stringRedisTemplate.opsForValue().get(key);
            if (null != json) {
                spot = JSONUtil.parse(json, discardStrategy.spotClazz());
                // 再次缓存
                cache(key, spot);
            }
        }
        return spot;
    }


    /**
     * 设置一级缓存一次性移除百分比
     * @param l1DiscardPercent   范围：[0.1-1.0]
     */
    public void setL1DiscardPercent(Float l1DiscardPercent) {
        this.l1DiscardPercent = Math.min(Math.max(l1DiscardPercent, 0.1F), 1.0F);
    }
}
