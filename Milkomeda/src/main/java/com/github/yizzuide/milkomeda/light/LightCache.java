package com.github.yizzuide.milkomeda.light;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.github.yizzuide.milkomeda.pulsar.PulsarHolder;
import com.github.yizzuide.milkomeda.util.JSONUtil;
import com.github.yizzuide.milkomeda.util.Polyfill;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.io.Serializable;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.TimeUnit;

/**
 * LightCache
 *
 * 缓存方式：超级缓存（当前线程引用，根据业务可选）| 一级缓存（内存缓存池，缓存个数可控）| 二级缓存（Redis）
 *
 * V：标识数据
 * E：缓存业务数据
 *
 * @since 1.8.0
 * @version 1.17.0
 * @author yizzuide
 * Create at 2019/06/28 13:33
 */
@Slf4j
public class LightCache implements Cache {
    /**
     * 默认一级缓存最大个数
     */
    private static final Integer DEFAULT_L1_MAX_COUNT = 64;

    /**
     * 默认一级缓存一次性移除百分比
     */
    private static final Float DEFAULT_L1_DISCARD_PERCENT = 0.1F;

    /**
     * 默认二缓存过期时间
     */
    private static final Long DEFAULT_L2_EXPIRE = -1L;

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
     * 只写入一级缓存, 默认为 {@code false}
     */
    @Setter
    @Getter
    private Boolean onlyCacheL1 = false;

    /**
     * 一级缓存丢弃策略，默认为HOT
     */
    @Getter
    private LightDiscardStrategy strategy;

    /**
     * 自定义一级缓存丢弃策略实现
     */
    @Setter
    @Getter
    private Class<Discard> strategyClass;

    /**
     * 一级缓存丢弃策略
     */
    private Discard discardStrategy;

    /**
     * 二级缓存过期时间，默认为永不过期，单位：秒
     */
    @Setter
    @Getter
    private Long l2Expire =  DEFAULT_L2_EXPIRE;

    /**
     * 超级缓存
     */
    @Getter
    private LightContext superCache = new LightContext();

    /**
     * 一级缓存容器
     */
    private Map<String, Spot<Serializable, Object>> cacheMap = new ConcurrentSkipListMap<>();

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
    public void set(Serializable id) {
        if (null == id) return;
        // 如果一级缓存没有数据，创建新的缓存数据对象
        if (cacheMap.size() == 0) {
            superCache.set(id);
            return;
        }

        // 从一级缓存获取
        Optional<Spot<Serializable, Object>> viableSpot = cacheMap.values()
                .stream()
                .filter(spot -> spot.getView().equals(id))
                .findFirst();
        // 没找到，创建新的缓存数据对象
        if (!viableSpot.isPresent()) {
            superCache.set(id);
            return;
        }

        Spot<Serializable, Object> spot = viableSpot.get();
        // 排行加分
        discardStrategy.ascend(spot);
        // 设置到超级缓存，保存相同缓存数据对象只有一份
        superCache.set(spot);
    }

    @Override
    public <E> Spot<Serializable, E> get() {
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
     *                  而是先通过<codObject>get()</codObject>获得，修改之后再传入，这样才能只存储一份数据
     */
    @SuppressWarnings("unchecked")
    @Override
    public void set(String key, Spot<Serializable, ?> spot) {
        // 如果是父类型，需要向下转型（会触发初始化排序状态字段）
        if (spot.getClass() == Spot.class) {
            spot = discardStrategy.deform(key, (Spot<Serializable, Object>) spot);
        }

        // 开始缓存
        cache(key, (Spot<Serializable, Object>)spot);
    }

    /**
     * 执行缓存
     * @param key   键
     * @param spot  缓存数据
     */
    private void cache(String key, Spot<Serializable, Object> spot) {
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
    private void cacheL2(String key, Spot<Serializable, Object> spot) {
        // 异步添加到Redis（由于序列化稍微耗时）
        PulsarHolder.getPulsar().post(() -> {
            String json = JSONUtil.serialize(spot);
            if (l2Expire > 0) {
                stringRedisTemplate.opsForValue().set(key, json, l2Expire, TimeUnit.SECONDS);
            } else {
                stringRedisTemplate.opsForValue().set(key, json);
            }
        });
    }

    /**
     * 一级缓存
     * @param key   键
     * @param spot  缓存数据
     */
    private void cacheL1(String key, Spot<Serializable, Object> spot) {
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
    public <E> Spot<Serializable, E> get(String key) {
        return get(key, null);
    }

    @Override
    public  <E> Spot<Serializable, E> get(String key, Class<Serializable> vClazz, Class<E> eClazz) {
        JavaType javaType = TypeFactory.defaultInstance()
                .constructParametricType(discardStrategy.spotClazz(), vClazz, eClazz);
        return get(key, javaType);
    }

    @Override
    public <E> Spot<Serializable, E> get(String key, TypeReference<Serializable> vTypeRef, TypeReference<E> eTypeRef) {
        // TypeReference -> (Type | JavaType) -> Class
        // TypeFactory.defaultInstance().constructType(SerializableTypeRef.getType()).getRawClass();
        JavaType vType =  TypeFactory.defaultInstance().constructType(vTypeRef);
        JavaType eType = TypeFactory.defaultInstance().constructType(eTypeRef);
        JavaType javaType = TypeFactory.defaultInstance()
                .constructParametricType(discardStrategy.spotClazz(), vType, eType);
        return get(key, javaType);
    }

    /**
     * 从缓存获取数据
     * @param key       缓存key
     * @param javaType  缓存数据类型，如果为null，则不支持复杂数据类型的反序列化
     * @return  Spot
     */
    @SuppressWarnings("unchecked")
    private <E> Spot<Serializable, E> get(String key, JavaType javaType) {
        // 从一级缓存查找
        Spot<Serializable, Object> spot = cacheMap.get(key);
        if (null != spot) {
            // 排行加分
            discardStrategy.ascend(spot);
            return (Spot<Serializable, E>) spot;
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
        return (Spot<Serializable, E>) spot;
    }

    @Override
    public void erase(String key) {
        // 从二级缓存移除
        Polyfill.redisDelete(stringRedisTemplate, key);
        // 从一级缓存移除
        cacheMap.remove(key);
    }

    /**
    * 设置一级缓存一次性移除百分比
    * @param l1DiscardPercent   范围：[0.1-1.0]
    */
    public void setL1DiscardPercent(Float l1DiscardPercent) {
        this.l1DiscardPercent = Math.min(Math.max(l1DiscardPercent, 0.1F), 1.0F);
    }

    public void setStrategy(LightDiscardStrategy strategy) {
        this.strategy = strategy;
        if (strategy == null) {
            discardStrategy  = new HotDiscard();
            return;
        }
        switch (strategy) {
            case HOT:
                discardStrategy  = new HotDiscard();
                break;
            case TIMELINE:
                discardStrategy = new TimelineDiscard();
                break;
            case CUSTOM:
            {
                if (strategyClass == null) {
                    discardStrategy  = new HotDiscard();
                    return;
                }
                try {
                    discardStrategy = strategyClass.newInstance();
                } catch (Exception e) {
                    log.error("light create strategy class error with message:{}", e.getMessage(), e);
                }
            }
        }
    }
}
