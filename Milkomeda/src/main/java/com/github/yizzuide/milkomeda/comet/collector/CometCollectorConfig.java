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

package com.github.yizzuide.milkomeda.comet.collector;

import com.github.yizzuide.milkomeda.comet.core.CometAspect;
import com.github.yizzuide.milkomeda.comet.core.CometConfig;
import com.github.yizzuide.milkomeda.comet.core.CometData;
import com.github.yizzuide.milkomeda.comet.core.CometHolder;
import com.github.yizzuide.milkomeda.pillar.PillarExecutor;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * CometCollectorConfig
 *
 * @author yizzuide
 * @since 3.0.0
 * @version 3.11.0
 * @see BeanFactoryUtils#beansOfTypeIncludingAncestors(org.springframework.beans.factory.ListableBeanFactory, java.lang.Class)
 * <br />
 * Create at 2020/03/28 18:54
 */
@Configuration
@AutoConfigureAfter(CometConfig.class)
@EnableConfigurationProperties(CometCollectorProperties.class)
@ConditionalOnProperty(prefix = "milkomeda.comet.collector", name = "enable", havingValue = "true")
public class CometCollectorConfig implements ApplicationContextAware {

    @Autowired(required = false)
    private List<Collector> collectors;

    @Bean
    public CollectorFactory collectorFactory() {
        PillarExecutor<CometData, Object> pillarExecutor = new PillarExecutor<>();
        // 排除tag-collector
        if (!CollectionUtils.isEmpty(collectors)) {
            pillarExecutor.addPillarList(collectors.stream()
                    .filter(c -> StringUtils.hasLength(c.supportType()))
                    .collect(Collectors.toList()));
        }
        return new CollectorFactory(pillarExecutor);
    }

    @Bean
    public CollectorRecorder collectorRecorder() {
        return new CollectorRecorder(collectorFactory());
    }

    @Autowired
    public void config(CometAspect cometAspect, CometCollectorProperties cometCollectorProperties) {
        // 设置日志采集器
        cometAspect.setRecorder(collectorRecorder());
        CometHolder.setCollectorProps(cometCollectorProperties);
    }

    @Override
    public void setApplicationContext(@NonNull ApplicationContext applicationContext) throws BeansException {
        // 如果为空，到BeanFactory里查找
        if (CollectionUtils.isEmpty(collectors)) {
            collectors = new ArrayList<>(BeanFactoryUtils.beansOfTypeIncludingAncestors(applicationContext, Collector.class).values());
            if (CollectionUtils.isEmpty(collectors)) {
                return;
            }
            // 排除tag-collector
            applicationContext.getBean(CollectorFactory.class).getPillarExecutor().addPillarList(collectors.stream().filter(c -> StringUtils.hasLength(c.supportType())).collect(Collectors.toList()));
        }
    }
}
