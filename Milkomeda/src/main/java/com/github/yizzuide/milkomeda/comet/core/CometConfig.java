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
import com.github.yizzuide.milkomeda.universe.context.ApplicationContextHolder;
import com.github.yizzuide.milkomeda.universe.extend.web.handler.HotHttpHandlerProperty;
import com.github.yizzuide.milkomeda.universe.extend.web.handler.NamedHandler;
import com.github.yizzuide.milkomeda.universe.metadata.BeanIds;
import com.github.yizzuide.milkomeda.universe.polyfill.SpringMvcPolyfill;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.web.servlet.WebMvcAutoConfiguration;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.lang.NonNull;
import org.springframework.util.CollectionUtils;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.util.*;

/**
 * CometConfig
 *
 * @author yizzuide
 * @since 2.0.0
 * @version 3.15.0
 * <br>
 * Create at 2019/12/12 18:10
 */
@Configuration
@AutoConfigureAfter({WebMvcAutoConfiguration.class, PulsarConfig.class})
@EnableConfigurationProperties({MilkomedaProperties.class, CometProperties.class})
public class CometConfig implements ApplicationListener<ApplicationStartedEvent> {

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

    @Bean
    public CometInterceptor cometInterceptor() {
        return new CometInterceptor();
    }

    @Bean
    @ConditionalOnProperty(prefix = "milkomeda.comet.request-interceptors.xss", name = "enable", havingValue = "true")
    public CometXssRequestInterceptor cometXssRequestInterceptor() {
        return new CometXssRequestInterceptor();
    }

    @Bean
    @ConditionalOnProperty(prefix = "milkomeda.comet.request-interceptors.sql-inject", name = "enable", havingValue = "true")
    public CometSqlInjectRequestInterceptor cometSqlInjectRequestInterceptor() {
        return new CometSqlInjectRequestInterceptor();
    }

    @Bean
    public CometResponseBodyAdvice cometResponseBodyAdvice() {
        return new CometResponseBodyAdvice();
    }

    @Override
    public void onApplicationEvent(@NonNull ApplicationStartedEvent event) {
        Map<String, HotHttpHandlerProperty> requestInterceptors = cometProperties.getRequestInterceptors();
        if (!CollectionUtils.isEmpty(requestInterceptors)) {
            Map<String, CometRequestInterceptor> requestInterceptorMap = ApplicationContextHolder.get().getBeansOfType(CometRequestInterceptor.class);
            if (!CollectionUtils.isEmpty(requestInterceptorMap)) {
                CometHolder.setRequestInterceptors(NamedHandler.sortedList(requestInterceptorMap, requestInterceptors::get));
            }
        }

        Map<String, HotHttpHandlerProperty> responseInterceptors = cometProperties.getResponseInterceptors();
        if (CollectionUtils.isEmpty(responseInterceptors)) {
            return;
        }
        Map<String, CometResponseInterceptor> responseInterceptorMap = ApplicationContextHolder.get().getBeansOfType(CometResponseInterceptor.class);
        if (CollectionUtils.isEmpty(responseInterceptorMap)) {
            return;
        }
        CometHolder.setResponseInterceptors(NamedHandler.sortedList(responseInterceptorMap, responseInterceptors::get));
    }


    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    @Configuration
    static class ExtendedConfig implements InitializingBean {

        @Autowired
        private RequestMappingHandlerAdapter adapter;

        // Spring Boot 2.7: Since Spring Framework 5.1, Spring MVC has supported multiple RequestMappingHandlerMapping beans.
        //  Spring Boot 2.7 no longer defines MVC’s main requestMappingHandlerMapping bean as @Primary.
        @Qualifier(BeanIds.REQUEST_MAPPING_HANDLER_MAPPING)
        @Autowired
        private RequestMappingHandlerMapping requestMappingHandlerMapping;

        @Autowired
        private HandlerExceptionResolver handlerExceptionResolver;

        @Autowired
        private CometResponseBodyAdvice cometResponseBodyAdvice;

        @Autowired
        private CometInterceptor cometInterceptor;

        @Override
        public void afterPropertiesSet() throws Exception {
            configParamResolve();
            configRequestMappingHandlerMapping();
            configResponseBodyAdvice();
        }

        // 配置RequestMappingHandlerAdapter实现动态注解SpringMVC处理器组件
        private void configParamResolve() {
            List<HandlerMethodArgumentResolver> argumentResolvers = new ArrayList<>();
            // 动态添加针对注解 @CometParam 处理的解析器
            argumentResolvers.add(new CometParamResolver());
            argumentResolvers.addAll(Objects.requireNonNull(adapter.getArgumentResolvers()));
            adapter.setArgumentResolvers(argumentResolvers);
        }

        // 动态添加内置拦截器
        private void configRequestMappingHandlerMapping() {
            SpringMvcPolyfill.addDynamicInterceptor(cometInterceptor,  Ordered.HIGHEST_PRECEDENCE, Collections.singletonList("/**"),
                    null, requestMappingHandlerMapping);
        }

        // 动态添加到响应处理
        private void configResponseBodyAdvice() {
            SpringMvcPolyfill.addDynamicResponseBodyAdvice(adapter.getReturnValueHandlers(), handlerExceptionResolver, cometResponseBodyAdvice);
        }
    }
}
