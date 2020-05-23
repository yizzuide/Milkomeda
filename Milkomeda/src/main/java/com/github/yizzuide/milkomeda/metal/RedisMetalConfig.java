package com.github.yizzuide.milkomeda.metal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.PatternTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter;

/**
 * RedisMetalConfig
 * 基于Redis的发布/订阅实现分布式配置动态刷新
 *
 * @author yizzuide
 * @since 3.6.0
 * @version 3.6.1
 * Create at 2020/05/22 16:34
 */
@EnableConfigurationProperties(MetalProperties.class)
@ConditionalOnClass(RedisTemplate.class)
@AutoConfigureAfter(RedisAutoConfiguration.class)
public class RedisMetalConfig {

    @Autowired
    private MetalProperties props;

    @Bean
    public RedisMessageListenerContainer container(RedisConnectionFactory connectionFactory,
                                                   MessageListenerAdapter listenerAdapter) {
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);
        container.addMessageListener(listenerAdapter, new PatternTopic(MetalMessageHandler.getTopic(props.getApplicationName())));
        return container;
    }

    @Bean
    public MessageListenerAdapter listenerAdapter(MetalMessageHandler metalMessageHandler) {
        return new MessageListenerAdapter(metalMessageHandler);
    }

    @Bean
    public MetalMessageHandler metalMessageHandler() {
        return new MetalMessageHandler();
    }

    @Autowired
    public void config(MetalMessageHandler metalMessageHandler) {
        MetalHolder.setMetalMessageHandler(metalMessageHandler);
    }
}
