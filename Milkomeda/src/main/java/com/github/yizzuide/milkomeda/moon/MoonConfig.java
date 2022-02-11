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

package com.github.yizzuide.milkomeda.moon;

import com.github.yizzuide.milkomeda.universe.context.WebContext;
import com.github.yizzuide.milkomeda.util.ReflectUtil;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull;
import org.springframework.util.CollectionUtils;

import java.util.List;

/**
 * MoonConfig
 *
 * @author yizzuide
 * @since 3.0.0
 * @version 3.7.0
 * Create at 2020/03/28 17:40
 */
@Slf4j
@Configuration
@EnableConfigurationProperties(MoonProperties.class)
public class MoonConfig implements ApplicationContextAware {

    @Autowired
    private MoonProperties moonProperties;

    @SneakyThrows
    @SuppressWarnings({"rawtypes", "unchecked"})
    @Override
    public void setApplicationContext(@NonNull ApplicationContext applicationContext) throws BeansException {
        List<MoonProperties.Instance> instances = moonProperties.getInstances();
        if (CollectionUtils.isEmpty(instances)) {
            return;
        }
        for (MoonProperties.Instance instance : instances) {
            String beanName = instance.getName();
            String cacheName = instance.getCacheName();
            if (instance.getMoonStrategyClazz() == null && instance.getType() != null) {
                switch (instance.getType()) {
                    case PERIODIC:
                        instance.setMoonStrategyClazz(PeriodicMoonStrategy.class);
                        break;
                    case PERCENT:
                        instance.setMoonStrategyClazz(PercentMoonStrategy.class);
                        break;
                }
            }
            Class<? extends MoonStrategy> moonStrategyClazz = instance.getMoonStrategyClazz();
            Moon moon = WebContext.registerBean((ConfigurableApplicationContext) applicationContext, beanName, Moon.class);
            moon.setCacheName(cacheName);
            try {
                MoonStrategy moonStrategy = moonStrategyClazz.newInstance();
                moon.setMoonStrategy(moonStrategy);
                moon.setMixinMode(moonProperties.isMixinMode());
                if (!CollectionUtils.isEmpty(instance.getProps())) {
                    ReflectUtil.setField(moonStrategy, instance.getProps());
                }
                if (moonStrategy instanceof AbstractMoonStrategy) {
                    // 读取lua脚本
                    AbstractMoonStrategy luaMoonStrategy = (AbstractMoonStrategy) moonStrategy;
                    String luaScript = luaMoonStrategy.loadLuaScript();
                    luaMoonStrategy.setLuaScript(luaScript);
                }
            } catch (Exception e) {
                log.error("Moon invoke error with msg: {}", e.getMessage(), e);
            }
            if (CollectionUtils.isEmpty(instance.getPhases())) {
                continue;
            }
            moon.add(instance.getPhases().toArray(new Object[0]));
        }
    }
}
