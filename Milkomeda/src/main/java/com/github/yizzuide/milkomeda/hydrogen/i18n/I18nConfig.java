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

package com.github.yizzuide.milkomeda.hydrogen.i18n;

import com.github.yizzuide.milkomeda.hydrogen.core.HydrogenHolder;
import com.github.yizzuide.milkomeda.universe.metadata.BeanIds;
import com.github.yizzuide.milkomeda.universe.polyfill.SpringMvcPolyfill;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.context.MessageSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.web.servlet.WebMvcAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.i18n.LocaleChangeInterceptor;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.util.Collections;

/**
 * I18nConfig
 *
 * @author yizzuide
 * @since 3.0.0
 * @version 3.14.0
 * @see org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport#requestMappingHandlerAdapter(org.springframework.web.accept.ContentNegotiationManager, org.springframework.format.support.FormattingConversionService, org.springframework.validation.Validator)
 * @see org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport#requestMappingHandlerMapping(org.springframework.web.accept.ContentNegotiationManager, org.springframework.format.support.FormattingConversionService, org.springframework.web.servlet.resource.ResourceUrlProvider)
 * @see org.springframework.web.servlet.handler.AbstractHandlerMapping
 * <br>
 * Create at 2019/08/02 15:24
 */
@Slf4j
@Configuration
@EnableConfigurationProperties(I18nProperties.class)
@AutoConfigureAfter({MessageSourceAutoConfiguration.class, WebMvcAutoConfiguration.class})
@ConditionalOnProperty(prefix = "milkomeda.hydrogen.i18n", name = "enable", havingValue = "true")
public class I18nConfig {
    // Springboot 2.6: The application’s MessageSource is now used when resolving {parameters} in constraint messages.
    //  This allows you to use your application’s messages.properties files for Bean Validation messages.
    @Autowired
    private MessageSource messageSource;

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

    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    @Configuration
    static class ExtendedConfig implements InitializingBean {

        @Autowired
        private I18nProperties i18nProperties;

        @Autowired
        private I18nMessages i18nMessages;

        @Qualifier(BeanIds.REQUEST_MAPPING_HANDLER_MAPPING)
        @Autowired
        private RequestMappingHandlerMapping requestMappingHandlerMapping;


        @Override
        public void afterPropertiesSet() throws Exception {
            HydrogenHolder.setI18nMessages(i18nMessages);
            configRequestMappingHandlerMapping();
        }

        // 动态添加拦截器
        private void configRequestMappingHandlerMapping() {
            final LocaleChangeInterceptor localeChangeInterceptor = new LocaleChangeInterceptor();
            localeChangeInterceptor.setParamName(i18nProperties.getQuery());
            SpringMvcPolyfill.addDynamicInterceptor(localeChangeInterceptor, Ordered.HIGHEST_PRECEDENCE + 10, Collections.singletonList("/**"),
                    null, requestMappingHandlerMapping);
        }
    }
}
