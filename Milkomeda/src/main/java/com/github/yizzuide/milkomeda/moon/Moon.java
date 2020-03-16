package com.github.yizzuide.milkomeda.moon;

import com.github.yizzuide.milkomeda.light.LightCachePut;
import com.github.yizzuide.milkomeda.light.LightCacheable;
import lombok.Data;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Moon
 *
 * @author yizzuide
 * @since 2.2.0
 * @version 2.7.2
 * Create at 2019/12/31 18:13
 */
@Data
public class Moon<T> {
    public static final String CACHE_NAME = "lightCacheMoon";

    /**
     * 缓存实例名
     */
    private String cacheName = CACHE_NAME;
    /**
     * 链表头指针
     */
    private MoonNode<T> header;
    /**
     * 链表记录指针
     */
    private MoonNode<T> pointer;
    /**
     * 链块连接指针
     */
    private MoonNode<T> next;
    /**
     * 链表长度
     */
    private int len;

    /**
     * 阶段列表
     */
    private List<T> phaseNames;

    /**
     * 阶段分配策略
     */
    private MoonStrategy moonStrategy = new PeriodicMoonStrategy();

    /**
     * 添加阶段名
     * @param phaseNames    阶段列表
     */
    @SafeVarargs
    public final void add(T... phaseNames) {
        this.setPhaseNames(new ArrayList<>(Arrays.asList(phaseNames)));
        this.setLen(phaseNames.length);
        for (int i = 0; i < this.getLen(); i++) {
            if (i == 0) {
                this.setHeader(new MoonNode<>());
                this.getHeader().setData(phaseNames[i]);
                this.setNext(this.getHeader());
                continue;
            }
            MoonNode<T> moonNode = new MoonNode<>();
            moonNode.setData(phaseNames[i]);
            this.getNext().setNext(moonNode);
            this.setNext(moonNode);
        }
        // 尾连首
        this.getNext().setNext(this.getHeader());
        // 指向首
        this.setPointer(this.getHeader());
    }

    /**
     * 无序并发获得当前阶段类型（不支持分布式）
     * @return 阶段类型值
     */
    public T getCurrentPhase() {
        return this.getMoonStrategy().getCurrentPhase(this);
    }

    /**
     * 根据key获取当前轮的当前阶段的数据值（并发线程安全）
     * @param key          缓存key，一个轮对应一个key
     * @param prototype    Moon实例原型
     * @param <T>          阶段的类型
     * @return  当前轮的当前阶段的类型值
     */
    public static <T> T getPhase(String key, Moon<T> prototype) {
        // 获取左手指月
        LeftHandPointer leftHandPointer = prototype.getLeftHandPointer(key);
        Integer p = leftHandPointer.getCurrent();
        T phase = prototype.getMoonStrategy().getPhase(key, p, prototype);
        // 开始左手指月，拔动月相
        prototype.pluckLeftHandPointer(key, leftHandPointer);
        return phase;
    }

    /**
     * 根据key拔动当前左手指月
     * @param key             缓存key
     * @param leftHandPointer 左手指月
     * @return LeftHandPointer
     */
    @LightCachePut(value = "#target.cacheName", keyPrefix = "moon:lhp-", key = "#key")
    protected LeftHandPointer pluckLeftHandPointer(String key, LeftHandPointer leftHandPointer) {
        return this.getMoonStrategy().pluck(this, leftHandPointer);
    }

    /**
     * 根据key获取当前左手指月（缓存默认为一天，仅缓存到CacheL2，自定义配置通过注册名为`lightCacheMoon`的Cache Bean
     * @param key 缓存key
     * @return LeftHandPointer
     */
    @LightCacheable(value = "#target.cacheName", keyPrefix = "moon:lhp-", key = "#key", onlyCacheL2 = true)
    protected LeftHandPointer getLeftHandPointer(String key) {
        // 无法从缓存中获取时，创建新的左手指月
        return new LeftHandPointer();
    }
}
