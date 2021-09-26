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
 * @version 2.0.6
 * @author yizzuide
 * Create at 2019/06/30 18:57
 */
@Data
public class LightContext {
    // 每个缓存实例都有自己的超级缓存
    private final ThreadLocal<Spot<Serializable, ?>> context = new ThreadLocal<>();

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
