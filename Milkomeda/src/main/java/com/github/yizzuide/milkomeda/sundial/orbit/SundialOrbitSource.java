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

package com.github.yizzuide.milkomeda.sundial.orbit;

import com.github.yizzuide.milkomeda.orbit.*;
import com.github.yizzuide.milkomeda.sundial.SundialProperties;
import com.github.yizzuide.milkomeda.util.CollectionsKt;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.core.env.Environment;
import org.springframework.util.CollectionUtils;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Orbit config extension of {@link OrbitSource}.
 *
 * @author yizzuide
 * @since 3.13.0
 * @version 3.15.0
 * <br>
 * Create at 2022/02/23 02:11
 */
// 虽然@OrbitSourceProvider有添加`@Component`注解，但由于默认的用户业务应用并不扫描这个路径，所以不会被Spring IoC所识别。
// 但是Orbit模块在BeanDefinition阶段注册了回调，通过手动依赖查找扫描的方式找到*.orbit路径下的这个注解。
@OrbitSourceProvider
public class SundialOrbitSource implements OrbitSource {

    @Override
    public List<OrbitNode> createNodes(Environment environment) {
        SundialProperties sundialProperties;
        try {
            sundialProperties = Binder.get(environment).bind(SundialProperties.PREFIX, SundialProperties.class).get();
        } catch (Exception ignore) {
            // not config, back!
            return Collections.emptyList();
        }
        if (CollectionUtils.isEmpty(sundialProperties.getStrategy())) {
            return Collections.emptyList();
        }
        // convert strategy config to orbit node
        return sundialProperties.getStrategy().stream()
                .map(strategy -> new AspectJOrbitNode(strategy.getPointcutExpression(), strategy.getKeyName(),
                        DataSourceOrbitAdvice.class, CollectionsKt.singletonMap(SundialProperties.Strategy.KEY_NAME, strategy.getKeyName())))
                .collect(Collectors.toList());
    }
}
