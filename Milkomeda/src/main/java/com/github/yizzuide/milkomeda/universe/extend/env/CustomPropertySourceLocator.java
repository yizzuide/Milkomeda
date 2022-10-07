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

package com.github.yizzuide.milkomeda.universe.extend.env;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.env.ConfigurableEnvironment;

/**
 * CustomPropertySourceLocator
 * 自定义属性源
 *
 * @author yizzuide
 * @since 3.0.1
 * @version 3.2.1
 * <br />
 * Create at 2020/04/11 10:48
 */
public class CustomPropertySourceLocator implements /*PropertySourceLocator,*/ EnvironmentPostProcessor {

    // 添加自定义属性来源方式一：实现PropertySourceLocator接口，把CustomPropertySourceLocator注册为Bean
    /*@Override
    public PropertySource<?> locate(Environment environment) {
        Map<String, Object> mapResource = new HashMap<>();
        mapResource.put("field", "value");
        return new MapPropertySource("milkomeda", mapResource);
    }*/

    // 添加自定义属性来源方式二：实现EnvironmentPostProcessor接口，把CustomPropertySourceLocator注册到spring.factories
    // Spring启动时发出ApplicationEnvironmentPreparedEvent事件，通过ConfigFileApplicationListener.onApplicationEnvironmentPreparedEvent(...)加载SPI配置的所有排好序的EnvironmentPostProcessor实例
    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        CollectionsPropertySource.addToEnvironment(environment);
        ConditionPropertySource.addToEnvironment(environment);
    }
}
