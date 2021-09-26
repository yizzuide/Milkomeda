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

package com.github.yizzuide.milkomeda.pillar;

/**
 * PillarState
 * 状态机if/else拆分，需要枚举实现（用于确定的状态，不需要配合其它类）
 *
 * @author yizzuide
 * @since 3.10.0
 * Create at 2020/07/02 17:29
 */
public interface PillarState {
    /**
     * 根据状态类型获取当前实例
     * @param state         状态机类型
     * @param type          枚举类
     * @param typeValues    枚举值
     * @param <T>           枚举类型
     * @return  枚举实例
     */
    static <T extends PillarState> T of(String state, Class<T> type, T[] typeValues) {
        for (T pillarState : typeValues) {
            if (pillarState.getState().equals(state)) {
                return pillarState;
            }
        }
        throw new IllegalArgumentException("Pillar Can't find state type: " + state);
    }

    /**
     * 获取当前状态值
     * @return  当前状态值
     */
    String getState();
}
