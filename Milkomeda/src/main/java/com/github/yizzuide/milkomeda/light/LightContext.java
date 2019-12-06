package com.github.yizzuide.milkomeda.light;

import lombok.Data;

import java.io.Serializable;

/**
 * LightContext
 *
 * 线程缓存上下文，可以配合<code>LightCache</code>当作超级缓存，也可以单独使用
 *
 * I：上下文id
 * E：上下文数据
 *
 * @since 1.9.0
 * @version 1.17.0
 * @author yizzuide
 * Create at 2019/06/30 18:57
 */
@Data
public class LightContext {

    private static final ThreadLocal<Spot<Serializable, ?>> context = new ThreadLocal<>();

    /**
     * 设置上下文id
     * @param id    上下文id
     */
    public void set(Serializable id) {
        Spot<Serializable, ?> spot = new Spot<>();
        spot.setView(id);
        set(spot);
    }

    /**
     * 设置上下文数据
     * @param spot  Spot
     */
    public void set(Spot<Serializable, ?> spot) {
        context.set(spot);
    }

    /***
     * 获取上下文数据
     * @param <E> 实体类型
     * @return  Spot
     */
    @SuppressWarnings("unchecked")
    public <E> Spot<Serializable, E> get() {
        return (Spot<Serializable, E>) context.get();
    }

    /**
     * 移除上下文数据
     */
    public void remove() {
        context.remove();
    }
}
