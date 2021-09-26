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

package com.github.yizzuide.milkomeda.particle;

import java.util.List;
import java.util.Map;

/**
 * LimiterConfigSelector
 *
 * @author yizzuide
 * @since 3.5.0
 * Create at 2020/05/14 17:47
 */
public class LimiterConfigSelector {

    /**
     * 查询组合链的配置Limiter
     * @param handlerClazz  当前限制器
     * @param chain         限制器链名
     * @param props         配置
     * @return  Limiter
     */
    public static ParticleProperties.Limiter barrierSelect(Class<? extends Limiter> handlerClazz, List<String> chain, ParticleProperties props) {
        for (Map.Entry<String, LimitHandler> entry : ParticleConfig.getCacheHandlerBeans().entrySet()) {
            if (entry.getValue().getClass() != handlerClazz) {
                continue;
            }
            if (!chain.contains(entry.getKey())) {
                continue;
            }
            return props.getLimiters().stream().filter(limiter -> limiter.getName().equals(entry.getKey())).findFirst().orElse(null);
        }
        return null;
    }
}
