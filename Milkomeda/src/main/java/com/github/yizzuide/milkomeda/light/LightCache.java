package com.github.yizzuide.milkomeda.light;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.github.yizzuide.milkomeda.pulsar.PulsarHolder;
import com.github.yizzuide.milkomeda.util.JSONUtil;
import com.github.yizzuide.milkomeda.universe.polyfill.RedisPolyfill;
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
 * 缓存方式：超级缓存（ThreadLocal）| 一级缓存（内存缓存池，缓存个数可控）| 二级缓存（Redis）
 *
 * V：标识数据
 * E：缓存业务数据
 *
 * @since 1.8.0
 * @version 2.7.0
 * @author yizzuide
 * Create at 2019/06/28 13:33
 */
@Slf4j
public class LightCache implements Cache {
    /**
     * 一级缓存最大个数
     */
    @Setter
    @Getter
    private Integer l1MaxCount;

    /**
     * 一级缓存一次性移除百分比
     */
    @Getter
    private Float l1DiscardPercent;

    /**
     * 一级缓存过期时间
     */
    @Setter
    @Getter
    private Long l1Expire;

    /**
     * 只写入一级缓存
     */
    @Setter
    @Getter
    private Boolean onlyCacheL1;

    /**
     * 一级缓存丢弃策略
     */
    @Getter
    private LightDiscardStrategy strategy;

    /**
     * 自定义一级缓存丢弃策略实现，使用自定义丢弃策略时需要指定
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
    private Long l2Expire;

    /**
     * 只写入二级缓存
     */
    @Setter
    @Getter
    private Boolean onlyCacheL2;

    /**
     * 超级缓存（每个Cache都有自己的超级缓存，互不影响）
     */
    @Getter
    private final LightContext superCache = new LightContext();

    /**
     * 一级缓存容器（内存池）
     */
    private final Map<String, Spot<Serializable, Object>> cacheMap = new ConcurrentSkipListMap<>();

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
        boolean isAbandon = discardStrategy.ascend(spot);
        // 丢弃缓存，重新设置数据
        if (isAbandon) {
            superCache.set(id);
            return;
        }
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
     *                  而是先通过<code>get()</code>获得，修改之后再传入，这样才能只存储一份数据
     */
    @SuppressWarnings("unchecked")
    @Override
    public void set(String key, Spot<Serializable, ?> spot) {
        // 如果是父类型，需要向下转型（会触发初始化排序状态字段）
        if (spot.getClass() == Spot.class) {
            spot = discardStrategy.deform(key, (Spot<Serializable, Object>) spot, l1Expire);
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
        boolean success = true;
        if (!onlyCacheL2) {
            success = cacheL1(key, spot);
        }

        // 二级缓存
        if (!onlyCacheL1 && success) {
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
     * @return 缓存是否成功
     */
    private boolean cacheL1(String key, Spot<Serializable, Object> spot) {
        // 一级缓存超出最大个数
        if ((cacheMap.size() + 1) > l1MaxCount) {
            // 根据选择的策略来丢弃数据
            discardStrategy.discard(cacheMap, l1DiscardPercent);
        }

        // 排行加分
        boolean isAbandon = discardStrategy.ascend(spot);
        // 缓存被识别为过期，使缓存失效
        if (isAbandon) {
            if (!onlyCacheL1) {
                // 从二级缓存移除
                RedisPolyfill.redisDelete(stringRedisTemplate, key);
            }
            return false;
        }

        // 添加到一级缓存池
        cacheMap.put(key, spot);
        return true;
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
        Spot<Serializable, Object> spot = null;
        // 从一级缓存查找
        if (!onlyCacheL2) {
            spot = cacheMap.get(key);
            if (null != spot) {
                // 排行加分
                boolean isAbandon = discardStrategy.ascend(spot);
                // 如果放弃缓存
                if (isAbandon) {
                    // 删除缓存
                    erase(key);
                    return null;
                }
                return (Spot<Serializable, E>) spot;
            }
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
                if (!onlyCacheL2) {
                    // 添加到一级缓存池，缓存失败，放弃从缓存中恢复
                    if (!cacheL1(key, spot)) {
                        return null;
                    }
                }
            }
        }
        return (Spot<Serializable, E>) spot;
    }

    @Override
    public void erase(String key) {
        if (!onlyCacheL1) {
            // 从二级缓存移除
            RedisPolyfill.redisDelete(stringRedisTemplate, key);
        }
        if (!onlyCacheL2) {
            // 从一级缓存移除
            cacheMap.remove(key);
        }
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
            case DEFAULT:
            case HOT:
                discardStrategy  = new HotDiscard();
                break;
            case TIMELINE:
                discardStrategy = new TimelineDiscard();
                break;
            case LazyExpire:
                discardStrategy = new LazyExpireDiscard();
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
                    discardStrategy  = new HotDiscard();
                    log.error("light create strategy class error with message:{}", e.getMessage(), e);
                }
            }
        }
    }

    /**
     * 从来源配置拷贝
     * @param other LightCache
     */
    public void copyFrom(LightCache other) {
        this.setL1MaxCount(other.getL1MaxCount());
        this.setL1DiscardPercent(other.getL1DiscardPercent());
        this.setL1Expire(other.getL1Expire());
        this.setStrategy(other.getStrategy());
        this.setStrategyClass(other.getStrategyClass());
        this.setOnlyCacheL1(other.getOnlyCacheL1());
        this.setL2Expire(other.getL2Expire());
        this.setOnlyCacheL2(other.getOnlyCacheL2());
    }

    /**
     * 配置实例
     * @param props LightCache
     */
    public void configFrom(LightProperties props) {
        this.setL1MaxCount(props.getL1MaxCount());
        this.setL1DiscardPercent(props.getL1DiscardPercent());
        this.setL1Expire(props.getL1Expire().getSeconds());
        this.setStrategy(props.getStrategy());
        this.setStrategyClass(props.getStrategyClass());
        this.setOnlyCacheL1(props.isOnlyCacheL1());
        this.setL2Expire(props.getL2Expire().getSeconds());
        this.setOnlyCacheL2(props.isOnlyCacheL2());
    }
}
