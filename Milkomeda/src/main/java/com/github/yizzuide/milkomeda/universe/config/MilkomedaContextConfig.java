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

package com.github.yizzuide.milkomeda.universe.config;

import com.github.yizzuide.milkomeda.universe.context.WebContext;
import com.github.yizzuide.milkomeda.universe.extend.env.Environment;
import com.github.yizzuide.milkomeda.universe.extend.web.handler.DelegatingContextFilter;
import jakarta.servlet.DispatcherType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.web.servlet.WebMvcAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.boot.web.servlet.filter.OrderedFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.util.PathMatcher;
import org.springframework.web.servlet.config.annotation.DelegatingWebMvcConfiguration;
import org.springframework.web.util.pattern.PathPatternParser;

import java.util.Collections;

/**
 * MilkomedaContextConfig
 *
 * @see org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport
 * @author yizzuide
 * @since 2.0.0
 * @version 4.0.0
 * <br>
 * Create at 2019/12/13 19:09
 */
@EnableConfigurationProperties(MilkomedaProperties.class)
@AutoConfigureAfter({WebMvcAutoConfiguration.class, DelegatingWebMvcConfiguration.class})
@EnableAspectJAutoProxy(exposeProxy = true)
@Configuration
public class MilkomedaContextConfig {

    @Autowired
    private MilkomedaProperties properties;

    @Bean
    public Environment env(PathMatcher mvcPathMatcher, PathPatternParser mvcPatternParser) {
        WebContext.setMvcPathMatcher(mvcPathMatcher);
        WebContext.setMvcPatternParser(mvcPatternParser);
        Environment environment = new Environment();
        environment.putAll(properties.getEnv());
        return environment;
    }

    @Bean
    public DelegatingContextFilter delegatingContextFilter() {
        return new DelegatingContextFilter();
    }

    @Bean
    public FilterRegistrationBean<DelegatingContextFilter> delegatingFilterRegistrationBean() {
        FilterRegistrationBean<DelegatingContextFilter> delegatingFilterRegistrationBean = new FilterRegistrationBean<>();
        // 设置代理注册的Bean
        delegatingFilterRegistrationBean.setFilter(delegatingContextFilter());
        delegatingFilterRegistrationBean.setUrlPatterns(Collections.singleton("/*"));
        // Order defaults to after OrderedRequestContextFilter
        // 解决无法从RequestContext获取信息的问题
        delegatingFilterRegistrationBean.setOrder(OrderedFilter.REQUEST_WRAPPER_FILTER_MAX_ORDER - 104);
        delegatingFilterRegistrationBean.setDispatcherTypes(DispatcherType.REQUEST, DispatcherType.ASYNC);
        return delegatingFilterRegistrationBean;
    }
}
