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

package com.github.yizzuide.milkomeda.orbit;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.StandardEnvironment;
import org.springframework.util.CollectionUtils;

import java.util.List;

/**
 * AbstractOrbitSource
 * 抽象的切面配置源扩展，其它模块实现需要添加到`spring.factories`里的`Environment Post Processors`
 *
 * @author yizzuide
 * @since 3.13.0
 * Create at 2022/02/26 01:04
 */
public abstract class AbstractOrbitSource implements OrbitSource, EnvironmentPostProcessor {
    /**
     * 所有实现共享的配置源
     */
    private static OrbitProperties orbitProperties;


    static OrbitProperties getOrbitProperties() {
        return orbitProperties;
    }

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        if (environment.getClass() == StandardEnvironment.class) {
            return;
        }
        // 只初始化一次
        if (orbitProperties == null) {
            try {
                orbitProperties = Binder.get(environment).bind(OrbitProperties.PREFIX, OrbitProperties.class).get();
            } catch (Exception e) {
                // 没用配置过Orbit，创建适用于Sundial使用的配置
                orbitProperties = new OrbitProperties();
            }
        }
        // 调用扩展配置
        List<OrbitNode> orbitNodes = createNodes(environment);
        if (CollectionUtils.isEmpty(orbitNodes)) {
            return;
        }
        orbitNodes.forEach(this::addNode);
    }

    /**
     * 添加切面节点
     * @param orbitNode OrbitNode
     */
    private void addNode(OrbitNode orbitNode) {
        OrbitProperties.Item item = new OrbitProperties.Item();
        item.setKeyName(orbitNode.getId());
        item.setPointcutExpression(orbitNode.getPointcutExpression());
        item.setAdviceClassName(orbitNode.getAdviceClass());
        item.setProps(orbitNode.getProps());
        getOrbitProperties().getInstances().add(item);
    }
}
