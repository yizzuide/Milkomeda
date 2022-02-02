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

package com.github.yizzuide.milkomeda.comet.core;

import com.github.yizzuide.milkomeda.pulsar.PulsarConfig;
import com.github.yizzuide.milkomeda.universe.config.MilkomedaProperties;
import com.github.yizzuide.milkomeda.universe.polyfill.SpringMvcPolyfill;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.web.servlet.WebMvcAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * CometConfig
 *
 * @author yizzuide
 * @since 2.0.0
 * @version 3.12.10
 * Create at 2019/12/12 18:10
 */
@Configuration
@AutoConfigureAfter({WebMvcAutoConfiguration.class, PulsarConfig.class})
@EnableConfigurationProperties({MilkomedaProperties.class, CometProperties.class})
public class CometConfig {

    @Autowired CometProperties cometProperties;

    @Autowired
    public void config(CometProperties cometProperties) {
        CometHolder.setProps(cometProperties);
    }

    @Bean
    public CometAspect cometAspect() {
        return new CometAspect();
    }

    @Bean
    //@ConditionalOnMissingBean // 识别类型：FilterRegistrationBean，会导致永远无法加载
    // 下面两方式在版本2.1.0推出，用于识别泛型类型：FilterRegistrationBean<CometRequestFilter>
    // @ConditionalOnMissingBean(parameterizedContainer = FilterRegistrationBean.class)
    public FilterRegistrationBean<CometRequestFilter> cometRequestFilter() {
        FilterRegistrationBean<CometRequestFilter> cometRequestFilter = new FilterRegistrationBean<>();
        cometRequestFilter.setFilter(new CometRequestFilter());
        cometRequestFilter.setName("cometRequestFilter");
        cometRequestFilter.setUrlPatterns(Collections.singleton("/*"));
        cometRequestFilter.setOrder(Ordered.HIGHEST_PRECEDENCE + 10);
        return cometRequestFilter;
    }

    // 配置RequestMappingHandlerAdapter实现动态注解SpringMVC处理器组件
    @Autowired
    public void configParamResolve(RequestMappingHandlerAdapter adapter) {
        List<HandlerMethodArgumentResolver> argumentResolvers = new ArrayList<>();
        // 动态添加针对注解 @CometParam 处理的解析器
        argumentResolvers.add(new CometParamResolver());
        argumentResolvers.addAll(Objects.requireNonNull(adapter.getArgumentResolvers()));
        adapter.setArgumentResolvers(argumentResolvers);
    }

    @Autowired
    public void configRequestMappingHandlerMapping(RequestMappingHandlerMapping requestMappingHandlerMapping) {
        // 使用内置拦截器
        SpringMvcPolyfill.addDynamicInterceptor(cometInterceptor(),  Ordered.HIGHEST_PRECEDENCE, Collections.singletonList("/**"),
                null, requestMappingHandlerMapping);
    }

    @Bean
    public CometInterceptor cometInterceptor() {
        return new CometInterceptor();
    }

    @Bean
    public CometResponseBodyAdvice cometResponseBodyAdvice() {
        return new CometResponseBodyAdvice();
    }

    @Autowired
    public void configResponseBodyAdvice(RequestMappingHandlerAdapter adapter, HandlerExceptionResolver handlerExceptionResolver) {
        CometResponseBodyAdvice collectorResponseBodyAdvice = cometResponseBodyAdvice();
        // 动态添加到响应处理
        SpringMvcPolyfill.addDynamicResponseBodyAdvice(adapter.getReturnValueHandlers(), handlerExceptionResolver, collectorResponseBodyAdvice);
    }
}
