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

package com.github.yizzuide.milkomeda.light;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.listener.PatternTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter;

/**
 * LightConfig
 *
 * @since 1.17.0
 * @version 3.21.0
 * @author yizzuide
 * <br>
 * Create at 2019/12/03 16:22
 */
@Configuration
@EnableConfigurationProperties(LightProperties.class)
public class LightConfig {

    @Bean
    @ConditionalOnMissingBean
    public LightCacheAspect lightCacheAspect() {
        return new LightCacheAspect();
    }

    @Bean(LightCacheAspect.DEFAULT_BEAN_NAME)
    public Cache lightCache(LightProperties props) {
        LightCache lightCache = new LightCache();
        lightCache.configFrom(props);
        return lightCache;
    }

    @Bean
    public RedisMessageListenerContainer lightListenerContainer(RedisConnectionFactory connectionFactory,
                                                                MessageListenerAdapter lightListenerAdapter) {
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);
        container.addMessageListener(lightListenerAdapter, new PatternTopic(LightMessageHandler.getTopic(null)));
        return container;
    }

    @Bean
    public MessageListenerAdapter lightListenerAdapter(LightMessageHandler lightMessageHandler) {
        return new MessageListenerAdapter(lightMessageHandler);
    }

    @Bean
    public LightMessageHandler lightMessageHandler() {
        return new LightMessageHandler();
    }

    @Bean
    @ConditionalOnMissingBean
    public LightCacheCleanAstrolabeHandler lightCacheCleanAstrolabeHandler(@Autowired(required = false) LightThreadLocalScope scope) {
        return new LightCacheCleanAstrolabeHandler(scope);
    }

    @Bean
    @ConditionalOnProperty(prefix = "milkomeda.light", name = "enable-light-thread-local-scope", havingValue = "true")
    public LightThreadLocalScope lightThreadLocalScope() {
        return new LightThreadLocalScope("lightThreadLocal");
    }
}
