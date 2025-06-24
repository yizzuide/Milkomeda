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
import com.github.yizzuide.milkomeda.comet.core.CometHolder;
import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ServiceLocatorFactoryBean;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * CometCollectorConfig
 *
 * @see BeanFactoryUtils#beansOfTypeIncludingAncestors(org.springframework.beans.factory.ListableBeanFactory, java.lang.Class)
 * @since 3.0.0
 * @version 4.0.0
 * @author yizzuide
 * <br>
 * Create at 2020/03/28 18:54
 */
@Configuration
@AutoConfigureAfter(CometConfig.class)
@EnableConfigurationProperties(CometCollectorProperties.class)
@ConditionalOnProperty(prefix = "milkomeda.comet.collector", name = "enable", havingValue = "true")
public class CometCollectorConfig implements InitializingBean {

    @Autowired
    private CometAspect cometAspect;

    @Autowired
    private CometCollectorProperties cometCollectorProperties;

    @SuppressWarnings("rawtypes")
    @Bean(name = "collectorFactory")
    public FactoryBean collectorFactory() {
        ServiceLocatorFactoryBean factoryBean = new ServiceLocatorFactoryBean();
        factoryBean.setServiceLocatorInterface(CollectorFactory.class);
        return factoryBean;
    }

    @Bean
    public CollectorRecorder collectorRecorder(@Autowired(required = false) CollectorFactory collectorFactory) {
        CollectorRecorder collectorRecorder = new CollectorRecorder(collectorFactory);
        cometAspect.setRecorder(collectorRecorder);
        return collectorRecorder;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        CometHolder.setCollectorProps(cometCollectorProperties);
    }
}
