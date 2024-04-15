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

import com.github.yizzuide.milkomeda.universe.config.MilkomedaProperties;
import com.github.yizzuide.milkomeda.universe.context.ApplicationContextHolder;
import com.github.yizzuide.milkomeda.universe.extend.converter.MapToCollectionConverter;
import com.github.yizzuide.milkomeda.util.ReflectUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.config.ConfigDataEnvironmentPostProcessor;
import org.springframework.boot.context.event.ApplicationEnvironmentPreparedEvent;
import org.springframework.boot.context.properties.bind.BindResult;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.context.ApplicationListener;
import org.springframework.core.Ordered;
import org.springframework.core.convert.converter.GenericConverter;
import org.springframework.core.convert.support.ConfigurableConversionService;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.StandardEnvironment;
import org.springframework.lang.NonNull;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Objects;

/**
 * Environment Prepared Listener
 *
 * @author yizzuide
 * @since 3.0.1
 * @version 3.20.0
 * @see org.springframework.boot.context.config.ConfigDataEnvironmentPostProcessor
 * @see org.springframework.boot.context.config.AnsiOutputApplicationListener
 * <br>
 * Create at 2020/04/11 11:56
 */
@Slf4j
public class EnvironmentApplicationListener implements ApplicationListener<ApplicationEnvironmentPreparedEvent>, Ordered {

    @Override
    public void onApplicationEvent(@NonNull ApplicationEnvironmentPreparedEvent event) {
        ConfigurableEnvironment environment = event.getEnvironment();
        if (environment.getClass() == StandardEnvironment.class) {
            return;
        }

        // StandardServletEnvironment or StandardReactiveEnvironment
        ApplicationContextHolder.setPendingConfigurableEnvironment(environment);

        // BeanWrapper binding ConversionService process:
        // 1.Spring Boot start setting ConversionService
        // org.springframework.boot.SpringApplication.configureEnvironment()
        //  -> environment.setConversionService(new ApplicationConversionService());
        //  -> context.getBeanFactory().setConversionService(context.getEnvironment().getConversionService());
        // 2.Spring Boot create BeanWrapper and set ConversionService
        // org.springframework.beans.factory.support.AbstractAutowireCapableBeanFactory.instantiateBean()
        //  -> AbstractBeanFactory.initBeanWrapper()
        //      -> bw.setConversionService(getConversionService());

        // Register Converter
        ConfigurableConversionService conversionService = environment.getConversionService();
        conversionService.addConverter(new MapToCollectionConverter(conversionService));
        BindResult<MilkomedaProperties> mkBindResult = Binder.get(environment).bind(MilkomedaProperties.PREFIX, MilkomedaProperties.class);
        if (mkBindResult.isBound()) {
            MilkomedaProperties milkomedaProperties = mkBindResult.get();
            List<Class<GenericConverter>> converters = milkomedaProperties.getRegisterConverters();
            if (!CollectionUtils.isEmpty(converters)) {
                converters.stream().map(ReflectUtil::newInstance).filter(Objects::nonNull).forEach(conversionService::addConverter);
            }
        }
        // Get and check conversionService
        // ((ConfigurableApplicationContext)ApplicationContextHolder.get()).getBeanFactory().getConversionService()

        // bind property
        boolean logEnable = false;
        BindResult<Boolean> logBindResult = Binder.get(environment).bind("milkomeda.show-log", Boolean.class);
        if (logBindResult.isBound()) {
            logEnable = logBindResult.get();
        }
        log.info("milkomeda log is {}", logEnable ? "enable" : "disable");
    }

    @Override
    public int getOrder() {
        // Apply after ConfigDataEnvironmentPostProcessor has called EnvironmentPostProcessors
        return ConfigDataEnvironmentPostProcessor.ORDER + 10;
    }
}
