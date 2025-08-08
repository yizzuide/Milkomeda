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

import com.github.yizzuide.milkomeda.light.LightContext;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 限制器状态数据
 *
 * @author yizzuide
 * @since 1.5.0
 * @version 4.0.0
 * <br>
 * Create at 2019/05/30 13:44
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Particle {

    public static final String LIGHT_CONTEXT_ID = "ParticleLightContext";

    /**
     * 状态类型
     */
    private Class<? extends Limiter> type;

    /**
     * 是否被限制
     */
    private boolean limited;

    /**
     * 结果值
     */
    private Object value;

    /**
     * 获取当前状态
     * @return Particle
     * @since 4.0.0
     */
    public static Particle getContext() {
        return LightContext.getValue(LIGHT_CONTEXT_ID);
    }

    static void setContext(Particle particle) {
        LightContext.setValue(particle, LIGHT_CONTEXT_ID);
    }

    static void clearContext() {
        LightContext.clearValue(LIGHT_CONTEXT_ID);
    }
}
