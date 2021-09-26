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

package com.github.yizzuide.milkomeda.universe.env;

import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.env.PropertiesPropertySource;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import java.util.Properties;

/**
 * Environment
 *
 * @author yizzuide
 * @since 3.1.0
 * Create at 2020/04/21 10:44
 */
public class Environment {

    // Spring可配置环境变量
    private ConfigurableEnvironment configurableEnvironment;

    // 附加配置源
    private final Properties properties = new Properties();

    public void setConfigurableEnvironment(ConfigurableEnvironment configurableEnvironment) {
        this.configurableEnvironment = configurableEnvironment;
        MutablePropertySources propertySources = configurableEnvironment.getPropertySources();
        propertySources.addLast(new PropertiesPropertySource("milkomedaProperties", properties));
    }

    /**
     * 添加自定义数据源配置
     * @param key   键
     * @param value 值
     */
    public void put(String key, String value) {
        this.properties.setProperty(key, value);
    }

    /**
     * 获取Spring环境变量
     * @param key   键
     * @return  如果key不存在，返回null
     */
    public @Nullable String get(String key) {
        if (this.configurableEnvironment == null) {
            return null;
        }
        return this.configurableEnvironment.getProperty(key);
    }

    /**
     * 获取配置源
     * @return  Properties
     */
    public @NonNull Properties getProperties() {
        return this.properties;
    }
}
