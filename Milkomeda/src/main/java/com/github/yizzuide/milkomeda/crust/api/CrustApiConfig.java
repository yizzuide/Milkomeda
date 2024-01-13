/*
 * Copyright (c) 2024 yizzuide All rights Reserved.
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

package com.github.yizzuide.milkomeda.crust.api;

import com.github.yizzuide.milkomeda.crust.AbstractCrust;
import com.github.yizzuide.milkomeda.crust.CrustProperties;
import com.github.yizzuide.milkomeda.crust.CrustURLMappingConfigurer;
import com.github.yizzuide.milkomeda.light.Cache;
import com.github.yizzuide.milkomeda.light.LightCache;
import com.github.yizzuide.milkomeda.universe.metadata.BeanIds;
import com.github.yizzuide.milkomeda.universe.polyfill.SpringMvcPolyfill;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.Ordered;
import org.springframework.util.CollectionUtils;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Crust config used for api service.
 *
 * @since 3.15.0
 * @author yizzuide
 * <br>
 * Create at 2022/12/07 00:12
 */
@Import(CrustURLMappingConfigurer.class)
@EnableConfigurationProperties(CrustProperties.class)
@Configuration
public class CrustApiConfig {

    @Autowired
    private CrustProperties crustProps;

    @Bean(AbstractCrust.BEAN_NAME)
    public AbstractCrust crustApi() {
        return new CrustApi();
    }

    @Bean
    public CrustInterceptor crustInterceptor() {
        return new CrustInterceptor();
    }

    @Bean(AbstractCrust.CODE_CATCH_NAME)
    @ConditionalOnProperty(prefix = "milkomeda.crust", name = "login-type", havingValue = "CODE")
    public Cache crustCodeLightCache() {
        LightCache lightCache = new LightCache();
        lightCache.setEnableSuperCache(false);
        lightCache.setOnlyCacheL2(true);
        lightCache.setL2Expire(crustProps.getCodeExpire().getSeconds());
        return lightCache;
    }

    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    @Configuration
    static class ExtendedConfig implements InitializingBean {

        @Autowired
        private CrustInterceptor crustInterceptor;

        @Autowired
        private CrustProperties crustProperties;

        @Qualifier(BeanIds.REQUEST_MAPPING_HANDLER_MAPPING)
        @Autowired
        private RequestMappingHandlerMapping requestMappingHandlerMapping;

        @Override
        public void afterPropertiesSet() throws Exception {
            List<String> allowURLs = new ArrayList<>(crustProperties.getPermitUrls());
                List<String> additionPermitUrls = crustProperties.getAdditionPermitUrls();
            if (!CollectionUtils.isEmpty(additionPermitUrls)) {
                allowURLs.addAll(additionPermitUrls);
            }
            SpringMvcPolyfill.addDynamicInterceptor(crustInterceptor,  Ordered.HIGHEST_PRECEDENCE, Collections.singletonList("/**"),
                    allowURLs, requestMappingHandlerMapping);
        }
    }
}
