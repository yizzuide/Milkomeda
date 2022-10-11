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

import com.github.yizzuide.milkomeda.universe.context.ApplicationContextHolder;
import com.github.yizzuide.milkomeda.universe.context.WebContext;
import com.github.yizzuide.milkomeda.universe.extend.env.Environment;
import com.github.yizzuide.milkomeda.universe.extend.web.handler.DelegatingContextFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.web.servlet.WebMvcAutoConfiguration;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.boot.web.servlet.filter.OrderedFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.util.PathMatcher;
import org.springframework.web.filter.DelegatingFilterProxy;
import org.springframework.web.util.UrlPathHelper;
import org.springframework.web.util.pattern.PathPatternParser;

import java.io.Serializable;
import java.util.Collections;

/**
 * MilkomedaContextConfig
 *
 * @author yizzuide
 * @since 2.0.0
 * @version 3.14.0
 * <br>
 * Create at 2019/12/13 19:09
 */
@SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
@Configuration
@AutoConfigureAfter(WebMvcAutoConfiguration.class)
public class MilkomedaContextConfig {

    @Autowired
    private MilkomedaProperties properties;

    @Bean
    @ConditionalOnMissingBean
    public Environment env() {
        Environment environment = new Environment();
        environment.putAll(properties.getEnv());
        return environment;
    }

    @Bean
    @ConditionalOnMissingBean
    public ApplicationContextHolder applicationContextHolder(Environment env) {
        ApplicationContextHolder applicationContextHolder = new ApplicationContextHolder();
        ApplicationContextHolder.setEnvironment(env);
        return applicationContextHolder;
    }

    @Autowired
    public void config(PathMatcher mvcPathMatcher, PathPatternParser mvcPatternParser, UrlPathHelper mvcUrlPathHelper) {
        WebContext.setMvcPathMatcher(mvcPathMatcher);
        WebContext.setMvcPatternParser(mvcPatternParser);
        WebContext.setUrlPathHelper(mvcUrlPathHelper);
    }

    @Bean
    public DelegatingContextFilter delegatingContextFilter() {
        return new DelegatingContextFilter();
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    @Bean
    public FilterRegistrationBean delegatingFilterRegistrationBean() {
        FilterRegistrationBean delegatingFilterRegistrationBean = new FilterRegistrationBean();
        // 设置代理注册的Bean
        delegatingFilterRegistrationBean.setFilter(new DelegatingFilterProxy("delegatingContextFilter"));
        delegatingFilterRegistrationBean.setName("delegatingContextFilter");
        delegatingFilterRegistrationBean.setUrlPatterns(Collections.singleton("/*"));
        // Order defaults to after OrderedRequestContextFilter
        // 解决无法从RequestContext获取信息的问题
        int order = OrderedFilter.REQUEST_WRAPPER_FILTER_MAX_ORDER - 104;
        delegatingFilterRegistrationBean.setOrder(order);
        return delegatingFilterRegistrationBean;
    }

    @Bean
    @ConditionalOnClass(name = "org.springframework.data.redis.core.RedisTemplate")
    public RedisTemplate<String, Serializable> jsonRedisTemplate(LettuceConnectionFactory redisConnectionFactory) {
        RedisTemplate<String, Serializable> template = new RedisTemplate<>();
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new GenericJackson2JsonRedisSerializer());
        template.setConnectionFactory(redisConnectionFactory);
        return template;
    }
}
