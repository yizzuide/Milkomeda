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

import com.github.yizzuide.milkomeda.orbit.AbstractOrbitSource;
import com.github.yizzuide.milkomeda.orbit.OrbitNode;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.core.env.ConfigurableEnvironment;

import java.util.List;
import java.util.stream.Collectors;

/**
 * OrbitAdditionSource
 * Orbit配置扩展
 *
 * @author yizzuide
 * @since 3.13.0
 * Create at 2022/02/23 02:11
 */
public class OrbitAdditionSource extends AbstractOrbitSource {

    @Override
    public List<OrbitNode> createNodes(ConfigurableEnvironment environment) {
        SundialProperties sundialProperties;
        try {
            sundialProperties = Binder.get(environment).bind(SundialProperties.PREFIX, SundialProperties.class).get();
        } catch (Exception ignore) {
            // 获取当前模块没有配置，直接返回
            return null;
        }
        // 构建配置源
        return sundialProperties.getStrategy().stream()
                .map(strategy -> OrbitNode.builder()
                        .id(strategy.getKeyName())
                        .pointcutExpression(strategy.getPointcutExpression())
                        .adviceClass(OrbitDataSourceAdvice.class)
                        .build().putPropKV(SundialProperties.Strategy.KEY_NAME, strategy.getKeyName()))
                .collect(Collectors.toList());
    }
}
