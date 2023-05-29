/*
 * Copyright (c) 2021 yizzuide All rights Reserved.
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.github.yizzuide.milkomeda.light;

import com.github.yizzuide.milkomeda.universe.context.ApplicationContextHolder;
import com.github.yizzuide.milkomeda.universe.context.SpringContext;
import io.netty.util.concurrent.FastThreadLocal;
import lombok.Data;
import org.springframework.context.ConfigurableApplicationContext;

import java.io.Serializable;

/**
 * 线程缓存上下文，可以配合<code>LightCache</code>当作超级缓存，也可以单独使用。
 *
 * @since 1.9.0
 * @version 3.15.0
 * @author yizzuide
 * <br>
 * Create at 2019/06/30 18:57
 */
@Data
public class LightContext<ID, V> {

    private final FastThreadLocal<Spot<ID, V>> context;

    public LightContext() {
        context = new FastThreadLocal<>();
    }

    public LightContext(FastThreadLocal<Spot<ID, V>> threadLocal) {
        this.context = threadLocal;
    }

    /**
     * 设置上下文id
     * @param id    上下文id
     */
    public void setId(ID id) {
        Spot<ID, V> spot = new Spot<>();
        spot.setView(id);
        set(spot);
    }

    /**
     * 设置上下文数据
     * @param data  上下文数据
     * @since 3.15.0
     */
    public void setData(V data) {
        Spot<ID, V> spot = new Spot<>();
        spot.setData(data);
        set(spot);
    }

    /**
     * 设置上下文数据
     * @param spot  Spot
     */
    public void set(Spot<ID, V> spot) {
        context.set(spot);
    }

    /***
     * 获取上下文数据
     * @return  Spot
     */
    public Spot<ID, V> get() {
        return context.get();
    }

    /**
     * 移除上下文数据
     */
    public void remove() {
        context.remove();
    }


    /**
     * 设置线程数据（用于注册的LightContext Bean）
     * @param value 任意对象
     * @param identifier 唯一标识
     * @param <V>   对象类型
     * @return LightContext
     * @since 3.13.0
     */
    @SuppressWarnings("unchecked")
    public static <V> LightContext<Serializable, V> setValue(V value, String identifier) {
        LightContext<Serializable, V> lightContext = SpringContext.registerBean((ConfigurableApplicationContext) ApplicationContextHolder.get(), identifier, LightContext.class);
        Spot<Serializable, V> spot = new Spot<>();
        spot.setData(value);
        lightContext.set(spot);
        return lightContext;
    }


    /**
     * 获取线程数据（用于注册的LightContext Bean）
     * @param identifier 唯一标识
     * @param <V>   对象类型
     * @return cache value
     * @since 3.13.0
     */
    @SuppressWarnings("unchecked")
    public static <V> V getValue(String identifier) {
        LightContext<Serializable, V> lightContext = SpringContext.registerBean((ConfigurableApplicationContext) ApplicationContextHolder.get(), identifier, LightContext.class);
        Spot<Serializable, V> spot = lightContext.get();
        return spot.getData();
    }
}
