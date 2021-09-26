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

package com.github.yizzuide.milkomeda.comet.collector;

import com.github.yizzuide.milkomeda.comet.core.CometData;
import com.github.yizzuide.milkomeda.pillar.Pillar;

/**
 * Collector
 * 日志收集器接口
 *
 * @author yizzuide
 * @since 1.15.0
 * @version 1.16.0
 * Create at 2019/11/13 18:12
 */
public interface Collector extends Pillar<CometData, Object> {

    @Override
    default void process(CometData params, Object result) {}

    /**
     * 数据准备
     * @param params CometData
     */
    void prepare(CometData params);

    /**
     * 执行成功
     * @param params CometData
     */
    void onSuccess(CometData params);

    /**
     * 执行失败
     * @param params CometData
     */
    void onFailure(CometData params);
}
