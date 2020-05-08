package com.github.yizzuide.milkomeda.universe.config;

import com.github.yizzuide.milkomeda.universe.context.ApplicationContextHolder;
import com.github.yizzuide.milkomeda.universe.context.WebContext;
import com.github.yizzuide.milkomeda.universe.env.Environment;
import com.github.yizzuide.milkomeda.universe.handler.DelegatingContextFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.web.servlet.WebMvcAutoConfiguration;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.util.PathMatcher;
import org.springframework.web.filter.DelegatingFilterProxy;
import org.springframework.web.util.UrlPathHelper;

import java.io.Serializable;
import java.util.Collections;

/**
 * MilkomedaContextConfig
 *
 * @author yizzuide
 * @since 2.0.0
 * @version 3.4.0
 * Create at 2019/12/13 19:09
 */
@Configuration
@AutoConfigureAfter(WebMvcAutoConfiguration.class)
public class MilkomedaContextConfig {

    @Bean
    @ConditionalOnMissingBean
    public Environment env() {
        return new Environment();
    }

    @Bean
    @ConditionalOnMissingBean
    public ApplicationContextHolder applicationContextHolder(Environment env) {
        ApplicationContextHolder applicationContextHolder = new ApplicationContextHolder();
        ApplicationContextHolder.setEnvironment(env);
        return applicationContextHolder;
    }

    @Autowired
    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    public void config(PathMatcher mvcPathMatcher, UrlPathHelper mvcUrlPathHelper) {
        WebContext.setMvcPathMatcher(mvcPathMatcher);
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
        delegatingFilterRegistrationBean.setOrder(Ordered.HIGHEST_PRECEDENCE + 40);
        return delegatingFilterRegistrationBean;
    }

    @Bean
    @ConditionalOnClass(name = "org.springframework.data.redis.core.RedisTemplate")
    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    public RedisTemplate<String, Serializable> jsonRedisTemplate(LettuceConnectionFactory redisConnectionFactory) {
        RedisTemplate<String, Serializable> template = new RedisTemplate<>();
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new GenericJackson2JsonRedisSerializer());
        template.setConnectionFactory(redisConnectionFactory);
        return template;
    }
}
