/*
 * Copyright (c) 2022 yizzuide All rights Reserved.
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

package com.github.yizzuide.milkomeda.sundial;

import com.github.yizzuide.milkomeda.orbit.OrbitProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.StandardEnvironment;

import java.util.HashMap;
import java.util.Map;

/**
 * OrbitAdaptor
 * Orbit配置桥接器
 *
 * @author yizzuide
 * @since 3.13.0
 * Create at 2022/02/23 02:11
 */
public class OrbitAdaptor implements EnvironmentPostProcessor {

    private static OrbitProperties orbitProperties;

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        if (environment.getClass() == StandardEnvironment.class) {
            return;
        }
        SundialProperties sundialProperties = null;
        try {
            sundialProperties = Binder.get(environment).bind(SundialProperties.PREFIX, SundialProperties.class).get();
            orbitProperties  = Binder.get(environment).bind(OrbitProperties.PREFIX, OrbitProperties.class).get();
        } catch (Exception e) {
            // 如果没有sundial模块配置，直接返回
            if (sundialProperties == null) {
                return;
            }
            // 没用配置过Orbit，创建适用于Sundial使用的配置
            orbitProperties = new OrbitProperties();
        }
        for (SundialProperties.Strategy strategy : sundialProperties.getStrategy()) {
            OrbitProperties.Item item = new OrbitProperties.Item();
            item.setKeyName(strategy.getKeyName());
            item.setPointcutExpression(strategy.getPointcutExpression());
            item.setAdviceClassName(OrbitDataSourceAdvice.class);
            Map<String, Object> props = new HashMap<>();
            props.put(SundialProperties.Strategy.KEY_NAME, strategy.getKeyName());
            item.setProps(props);
            orbitProperties.getInstances().add(item);
        }
    }

    public static OrbitProperties getOrbitProperties() {
        return orbitProperties;
    }
}
