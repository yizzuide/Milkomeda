package com.github.yizzuide.milkomeda.light;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.github.yizzuide.milkomeda.pulsar.PulsarHolder;
import com.github.yizzuide.milkomeda.util.JSONUtil;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentSkipListMap;

/**
 * LightCache
 *
 * 缓存方式：超级缓存（当前线程引用，根据业务可选）| 一级缓存（内存缓存池，缓存个数可控）| 二级缓存（Redis）
 *
 * V：标识数据
 * E：缓存业务数据
 *
 * @since 1.8.0
 * @version 1.9.0
 * @author yizzuide
 * Create at 2019/06/28 13:33
 */
public class LightCache<V, E> implements Cache<V, E> {
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
     * 超级缓存
     */
    @Getter
    private LightContext<V, E> superCache = new LightContext<>();

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
     * 设置超级缓存
     *
     * 如果在一级缓存池里根据缓存标识符可以取得缓存数据，则不会创建新的缓存数据对象
     *
     * @param id    缓存标识符
     */
    @Override
    public void set(V id) {
        if (null == id) return;
        // 如果一级缓存没有数据，创建新的缓存数据对象
        if (cacheMap.size() == 0) {
            superCache.set(id);
            return;
        }

        // 从一级缓存获取
        Optional<Spot<V, E>> viableSpot = cacheMap.values()
                .stream()
                .filter(spot -> spot.getView().equals(id))
                .findFirst();
        // 没找到，创建新的缓存数据对象
        if (!viableSpot.isPresent()) {
            superCache.set(id);
            return;
        }

        Spot<V, E> spot = viableSpot.get();
        // 排行加分
        discardStrategy.ascend(spot);
        // 设置到超级缓存，保存相同缓存数据对象只有一份
        superCache.set(spot);
    }

    @Override
    public Spot<V, E> get() {
        return superCache.get();
    }

    @Override
    public void remove() {
        superCache.remove();
    }

    /**
     * 存入一级缓存、二级缓存
     * @param key       键
     * @param spot      缓存数据，如果有设置过超级缓存，这个对象不应该通过new再次创建，
     *                  而是先通过<code>get()</code>获得，修改之后再传入，这样才能只存储一份数据
     */
    @Override
    public void set(String key, Spot<V, E> spot) {
        // 如果是父类型，需要向下转型（会触发初始化排序状态字段）
        if (spot.getClass() == Spot.class) {
            spot = discardStrategy.deform(key, spot);
        }

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

        // 排行加分
        discardStrategy.ascend(spot);

        // 添加到一级缓存池
        cacheMap.put(key, spot);
    }

    @Override
    public Spot<V, E> get(String key) {
        return get(key, null);
    }

    @Override
    public Spot<V, E> get(String key, Class<V> vClazz, Class<E> eClazz) {
        JavaType javaType = TypeFactory.defaultInstance()
                .constructParametricType(discardStrategy.spotClazz(), vClazz, eClazz);
        return get(key, javaType);
    }

    @Override
    public Spot<V, E> get(String key, TypeReference<V> vTypeRef, TypeReference<E> eTypeRef) {
        // TypeReference -> (Type | JavaType) -> Class
        // TypeFactory.defaultInstance().constructType(vTypeRef.getType()).getRawClass();
        JavaType vType =  TypeFactory.defaultInstance().constructType(vTypeRef);
        JavaType eType = TypeFactory.defaultInstance().constructType(eTypeRef);
        JavaType javaType = TypeFactory.defaultInstance()
                .constructParametricType(discardStrategy.spotClazz(), vType, eType);
        return get(key, javaType);
    }

    @SuppressWarnings("unchecked")
    private Spot<V, E> get(String key, JavaType javaType) {
        // 从一级缓存查找
        Spot<V, E> spot = cacheMap.get(key);
        if (null != spot) {
            // 排行加分
            discardStrategy.ascend(spot);
            return spot;
        }

        // 从二级缓存中查找
        if (!onlyCacheL1) {
            String json = stringRedisTemplate.opsForValue().get(key);
            if (null != json) {
                if (null != javaType) {
                    spot = JSONUtil.nativeRead(json, javaType);
                } else {
                    spot = JSONUtil.parse(json, discardStrategy.spotClazz());
                }
                // 添加到一级缓存池
                cacheL1(key, spot);
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
