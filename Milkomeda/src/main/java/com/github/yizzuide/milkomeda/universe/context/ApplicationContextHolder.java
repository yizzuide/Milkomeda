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

package com.github.yizzuide.milkomeda.universe.context;

import com.github.yizzuide.milkomeda.universe.extend.env.Environment;
import lombok.Getter;
import lombok.Setter;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.lang.NonNull;

/**
 * ApplicationContextHolder
 *
 * @author yizzuide
 * @since 0.2.1
 * @version 3.20.0
 * <br>
 * Create at 2019/04/12 11:04
 */
public class ApplicationContextHolder implements ApplicationContextAware {

    private static ApplicationContextHolder INSTANCE;

    // Spring准备好的环境变量
    @Setter @Getter
    private static ConfigurableEnvironment pendingConfigurableEnvironment;

    // 环境变量包装类
    @Setter @Getter
    private static Environment environment;

    @Getter
    private ApplicationContext applicationContext;

    public ApplicationContextHolder(Environment environment) {
        ApplicationContextHolder.setEnvironment(environment);
        INSTANCE = this;
    }

    @Override
    public void setApplicationContext(@NonNull ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
        if (pendingConfigurableEnvironment != null) {
            ApplicationContextHolder.getEnvironment().setConfigurableEnvironment(pendingConfigurableEnvironment);
            return;
        }
        if (applicationContext instanceof ConfigurableApplicationContext) {
            ConfigurableEnvironment configurableEnvironment = (ConfigurableEnvironment) applicationContext.getEnvironment();
            ApplicationContextHolder.getEnvironment().setConfigurableEnvironment(configurableEnvironment);
        }
    }

    /**
     * Try to get Spring Ioc Context.
     * @return null if `ApplicationContext` not created yet.
     * @since 3.15.0
     */
    public static ApplicationContext tryGet() {
        if (INSTANCE == null) {
            return null;
        }
        return get();
    }

    /**
     * 获取Spring Ioc上下文
     * @return ApplicationContext
     */
    public static ApplicationContext get() {
        return INSTANCE.getApplicationContext();
    }
}
