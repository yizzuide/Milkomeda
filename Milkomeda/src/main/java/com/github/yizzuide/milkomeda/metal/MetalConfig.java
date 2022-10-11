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

package com.github.yizzuide.milkomeda.metal;

import org.springframework.context.annotation.Bean;

/**
 * MetalConfig
 *
 * @author yizzuide
 * @since 3.6.0
 * <br>
 * Create at 2020/05/21 23:26
 */
public class MetalConfig {

    @Bean
    public MetalSource metalSource() {
        return new MetalSource();
    }

    @Bean
    public MetalContainer metalContainer(MetalSource metalSource) {
        MetalContainer metalContainer = new MetalContainer(metalSource);
        MetalHolder.setMetalContainer(metalContainer);
        return metalContainer;
    }

    @Bean
    public MetalRegister metalRegister(MetalContainer metalContainer) {
        return new MetalRegister(metalContainer);
    }

}
