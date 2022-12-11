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

package com.github.yizzuide.milkomeda.hydrogen.uniform;

import com.github.yizzuide.milkomeda.universe.config.MilkomedaContextConfig;
import com.github.yizzuide.milkomeda.universe.context.ApplicationContextHolder;
import com.github.yizzuide.milkomeda.universe.polyfill.SpringMvcPolyfill;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.boot.autoconfigure.web.servlet.error.ErrorMvcAutoConfiguration;
import org.springframework.boot.autoconfigure.web.servlet.error.ErrorViewResolver;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.error.ErrorAttributes;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport;

import java.util.stream.Collectors;

/**
 * UniformConfig
 *
 * @author yizzuide
 * @since 3.0.0
 * @version 3.14.0
 * @see WebMvcConfigurationSupport#handlerExceptionResolver(org.springframework.web.accept.ContentNegotiationManager)
 * #see ExceptionHandlerExceptionResolver#initExceptionHandlerAdviceCache()
 * #see ExceptionHandlerExceptionResolver#getExceptionHandlerMethod(org.springframework.web.method.HandlerMethod, java.lang.Exception)
 * <br>
 * Create at 2020/03/25 22:46
 */
@Import(MilkomedaContextConfig.class)
@AutoConfigureBefore(ErrorMvcAutoConfiguration.class)
@EnableConfigurationProperties({UniformProperties.class, ServerProperties.class})
@ConditionalOnProperty(prefix = "milkomeda.hydrogen.uniform", name = "enable", havingValue = "true")
@Configuration
public class UniformConfig {
    @Autowired
    private ServerProperties serverProperties;

    @Bean
    public UniformHandler uniformHandler() {
        return new UniformHandler();
    }

    @Bean
    public UniformResponseInterceptor uniformResponseInterceptor() {
        return new UniformResponseInterceptor();
    }

    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    @Bean
    public UniformErrorController uniformErrorController(ErrorAttributes errorAttributes,
                                                         ObjectProvider<ErrorViewResolver> errorViewResolvers) {
        return new UniformErrorController(errorAttributes, this.serverProperties.getError(),
                errorViewResolvers.orderedStream().collect(Collectors.toList()));
    }

    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    @Configuration
    static class ExtendedConfig implements InitializingBean {
        // 注入需要使用的ApplicationContext（让MilkomedaContextConfig先配置）
        @SuppressWarnings("unused")
        @Autowired
        private ApplicationContextHolder applicationContextHolder;

        @Autowired
        private UniformProperties props;

        @Autowired
        private HandlerExceptionResolver handlerExceptionResolver;

        @Override
        public void afterPropertiesSet() throws Exception {
            UniformHolder.setProps(props);
            configExceptionAdvice();
        }

        // 动态添加异常切面
        private void configExceptionAdvice() {
            SpringMvcPolyfill.addDynamicExceptionAdvice(handlerExceptionResolver, applicationContextHolder.getApplicationContext(),  "uniformHandler");
        }
    }
}
