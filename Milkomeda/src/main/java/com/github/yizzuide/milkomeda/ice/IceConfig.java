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

package com.github.yizzuide.milkomeda.ice;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * IceConfig
 *
 * @author yizzuide
 * @since 1.15.0
 * @version 3.8.0
 * <br />
 * Create at 2019/11/16 17:19
 */
@Configuration
public class IceConfig extends IceBasicConfig {
    @Bean
    @ConditionalOnProperty(prefix = "milkomeda.ice", name = "enable-task", havingValue = "true")
    public IceContext iceContext() {
        return new IceContext();
    }

    @Bean
    @ConditionalOnProperty(prefix = "milkomeda.ice", name = "enable-job-timer", havingValue = "true", matchIfMissing = true)
    public DelegatingDelayJobHandler delegatingDelayJobHandler() {
        return new DelegatingDelayJobHandler();
    }

    @Bean
    @ConditionalOnProperty(prefix = "milkomeda.ice", name = "enable-job-timer", havingValue = "true", matchIfMissing = true)
    public DelayTimer delayTimer() {
        return new DelayTimer();
    }
}
