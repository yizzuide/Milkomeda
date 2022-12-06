/*
 * Copyright (c) 2022 yizzuide All rights Reserved.
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

package com.github.yizzuide.milkomeda.crust;

import com.github.yizzuide.milkomeda.universe.polyfill.SpringMvcPolyfill;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import javax.annotation.Resource;
import java.util.Collections;

/**
 * Crust config for microservice.
 *
 * @since 3.15.0
 * @author yizzuide
 * <br>
 * Create at 2022/12/07 00:12
 */
@ConditionalOnClass(AuthenticationManager.class)
@EnableConfigurationProperties(CrustProperties.class)
@Configuration
public class CrustMicroConfig {
    @Bean
    public Crust crust() {
        return new Crust();
    }

    @Bean
    public CrustInterceptor crustInterceptor() {
        return new CrustInterceptor();
    }

    @Configuration
    static class ExtendedConfig implements InitializingBean {
        @Resource
        private Crust crust;

        @Resource
        private CrustInterceptor crustInterceptor;

        @Qualifier("requestMappingHandlerMapping")
        @Autowired
        private RequestMappingHandlerMapping requestMappingHandlerMapping;

        @Override
        public void afterPropertiesSet() throws Exception {
            CrustContext.set(crust);
            SpringMvcPolyfill.addDynamicInterceptor(crustInterceptor,  Ordered.HIGHEST_PRECEDENCE + 1, Collections.singletonList("/**"),
                    null, requestMappingHandlerMapping);
        }
    }
}
