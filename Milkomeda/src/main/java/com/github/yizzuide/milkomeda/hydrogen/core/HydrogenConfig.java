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

package com.github.yizzuide.milkomeda.hydrogen.core;

import com.github.yizzuide.milkomeda.hydrogen.filter.FilterConfig;
import com.github.yizzuide.milkomeda.hydrogen.i18n.I18nConfig;
import com.github.yizzuide.milkomeda.hydrogen.interceptor.InterceptorConfig;
import com.github.yizzuide.milkomeda.hydrogen.transaction.TransactionConfig;
import com.github.yizzuide.milkomeda.hydrogen.uniform.UniformConfig;
import com.github.yizzuide.milkomeda.hydrogen.validator.ValidatorConfig;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * HydrogenConfig
 *
 * @author yizzuide
 * @since 3.0.0
 * @version 3.2.0
 * <br />
 * Create at 2020/03/25 17:46
 */
@Configuration
@Import({
        TransactionConfig.class,
        UniformConfig.class,
        ValidatorConfig.class,
        I18nConfig.class,
        InterceptorConfig.class,
        FilterConfig.class
})
public class HydrogenConfig {
    // 在Spring Cloud环境下启用桥接事件监听
    @Bean
    @ConditionalOnClass(name = "org.springframework.cloud.context.environment.EnvironmentChangeEvent")
    public DelegatingEnvironmentChangeListener environmentChangeListener() {
        return new DelegatingEnvironmentChangeListener();
    }
}
