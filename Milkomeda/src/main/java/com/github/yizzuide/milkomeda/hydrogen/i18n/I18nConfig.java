package com.github.yizzuide.milkomeda.hydrogen.i18n;

import com.github.yizzuide.milkomeda.hydrogen.core.HydrogenHolder;
import com.github.yizzuide.milkomeda.hydrogen.core.HydrogenProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.context.MessageSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.web.servlet.WebMvcAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.handler.MappedInterceptor;
import org.springframework.web.servlet.i18n.LocaleChangeInterceptor;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.List;

/**
 * Config
 *
 * @author yizzuide
 * @since 2.8.0
 * @see org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport#requestMappingHandlerAdapter(org.springframework.web.accept.ContentNegotiationManager, org.springframework.format.support.FormattingConversionService, org.springframework.validation.Validator)
 * @see org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport#requestMappingHandlerMapping(org.springframework.web.accept.ContentNegotiationManager, org.springframework.format.support.FormattingConversionService, org.springframework.web.servlet.resource.ResourceUrlProvider)
 * @see org.springframework.web.servlet.handler.AbstractHandlerMapping
 * Create at 2019/08/02 15:24
 */
@Slf4j
@Configuration
@EnableConfigurationProperties(HydrogenProperties.class)
@AutoConfigureAfter({MessageSourceAutoConfiguration.class, WebMvcAutoConfiguration.class})
@ConditionalOnProperty(prefix = "milkomeda.hydrogen.i18n", name = "enable", havingValue = "true")
public class I18nConfig {

    @Autowired
    private MessageSource messageSource;

    @Autowired
    public void config(I18nMessages i18nMessages) {
        HydrogenHolder.setI18nMessages(i18nMessages);
    }

    // 在web应用程序上下文中注册一个LocaleResolver类型，必需设置Bean名称设置为localeResolver
    // 在DispatcherServlet源码中获取：this.localeResolver = context.getBean(LOCALE_RESOLVER_BEAN_NAME, LocaleResolver.class);
    @Bean
    public LocaleResolver localeResolver() {
        return new I18nLocaleResolver();
    }

    @Bean
    public I18nMessages i18nMessages() {
        return new I18nMessages(messageSource);
    }

    // 使用内置拦截器，实现动态注册
    @Autowired
    @SuppressWarnings("all")
    public void configInterceptors(RequestMappingHandlerMapping requestMappingHandlerMapping) {
        final LocaleChangeInterceptor localeChangeInterceptor = new LocaleChangeInterceptor();
        localeChangeInterceptor.setParamName("lang");
        String[] include = StringUtils.toStringArray(Collections.singletonList("/**"));
        // Interceptor -> MappedInterceptor
        MappedInterceptor mappedInterceptor = new MappedInterceptor(include, localeChangeInterceptor);
        // 下面这行可以省略，但为了保持内部的处理流程，使表达式成立：interceptors.count() == adaptedInterceptors.count()
        requestMappingHandlerMapping.setInterceptors(mappedInterceptor);
        try {
            // 虽然用了反射，但这些代码在只在启动时加载
            // 查找继承链 RequestMappingHandlerMapping -> RequestMappingInfoHandlerMapping -> AbstractHandlerMethodMapping -> AbstractHandlerMapping
            // TODO <mark> 由于使用底层API, 这个AbstractHandlerMapping.adaptedInterceptors很后的版本可能会改
            Field field = requestMappingHandlerMapping.getClass().getSuperclass().getSuperclass()
                    .getSuperclass().getDeclaredField("adaptedInterceptors");
            field.setAccessible(true);
            // 添加到可采纳的拦截器列表，让拦截器处理器Chain流程获取得到这个拦截器
            List<HandlerInterceptor> handlerInterceptors = (List<HandlerInterceptor>) field.get(requestMappingHandlerMapping);
            handlerInterceptors.add(mappedInterceptor);
            field.set(requestMappingHandlerMapping, handlerInterceptors);
        } catch (Exception e) {
            log.error("Hydrogen i18n invoke AbstractHandlerMapping.adaptedInterceptors error with msg: {}",  e.getMessage(), e);
        }
    }
}
