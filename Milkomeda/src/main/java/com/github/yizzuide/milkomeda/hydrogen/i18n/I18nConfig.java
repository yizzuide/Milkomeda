package com.github.yizzuide.milkomeda.hydrogen.i18n;

import com.github.yizzuide.milkomeda.hydrogen.core.HydrogenHolder;
import com.github.yizzuide.milkomeda.hydrogen.core.HydrogenProperties;
import com.github.yizzuide.milkomeda.universe.polyfill.SpringMvcPolyfill;
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
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.i18n.LocaleChangeInterceptor;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.util.Collections;

/**
 * I18nConfig
 *
 * @author yizzuide
 * @since 3.0.0
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

    @Autowired
    @SuppressWarnings("all")
    public void configRequestMappingHandlerMapping(RequestMappingHandlerMapping requestMappingHandlerMapping) {
        // 使用内置拦截器
        final LocaleChangeInterceptor localeChangeInterceptor = new LocaleChangeInterceptor();
        localeChangeInterceptor.setParamName("lang");
        SpringMvcPolyfill.addDynamicInterceptor(localeChangeInterceptor, null, Collections.singletonList("/**"),
                null, requestMappingHandlerMapping);
    }
}
