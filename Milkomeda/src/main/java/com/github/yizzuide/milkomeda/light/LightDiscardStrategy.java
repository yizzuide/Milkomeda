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

/**
 * LightDiscardStrategy
 *
 * @author yizzuide
 * @version 1.17.0
 * @version 2.0.1
 * <br />
 * Create at 2019/12/03 16:30
 */
public enum LightDiscardStrategy {
    /**
     * 默认策略（默认为HOT）
     */
    DEFAULT,
    /**
     * 热点策略，LFU（适用于大量相同类型记录数据的情况，使用得越频繁越不容易过期）
     */
    HOT,
    /**
     * 时间线策略，LRU（适用于大量相同类型记录数据的情况，最新使用的数据不会被丢弃）
     */
    TIMELINE,
    /**
     * 懒惰过期丢弃策略（轻量级过期方案，适用于缓存数据会在一定时间会被更新的情况）
     */
    LazyExpire,
    /**
     * 自定义策略
     */
    CUSTOM
}
