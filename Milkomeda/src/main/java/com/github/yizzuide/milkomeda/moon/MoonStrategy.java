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

package com.github.yizzuide.milkomeda.moon;

/**
 * MoonStrategy
 *
 * @author yizzuide
 * @since 2.6.0
 * @version 3.7.0
 * <br />
 * Create at 2020/03/13 21:16
 */
public interface MoonStrategy {

    /**
     * 无序并发获得当前阶段类型（不支持分布式）
     * @param moon Moon
     * @param <T> 阶段类型
     * @return 阶段值
     */
    <T> T getCurrentPhase(Moon<T> moon);

    /**
     * 获得环值（分布式并发安全）
     * @param key          缓存key，一个环对应一个key
     * @param p            当前阶段值
     * @param prototype    Moon实例原型
     * @param <T>          阶段的类型
     * @return  当前环的当前阶段值
     */
    <T> T getPhase(String key, Integer p, Moon<T> prototype);

    /**
     * 拔动月环
     * @param moon Moon实例
     * @param leftHandPointer 月相指针
     * @return 修改后月相指针
     */
    LeftHandPointer pluck(Moon<?> moon, LeftHandPointer leftHandPointer);

    /**
     * 执行lua流程脚本获得环值
     * @param key       缓存key，一个环对应一个key  
     * @param prototype Moon实例原型
     * @param <T>       阶段的类型
     * @return  当前环的当前阶段值
     * @since 3.7.0
     */
    <T> T getPhaseFast(String key, Moon<T> prototype);
}
