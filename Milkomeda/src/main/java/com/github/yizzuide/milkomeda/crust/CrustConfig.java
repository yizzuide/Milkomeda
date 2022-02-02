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

package com.github.yizzuide.milkomeda.crust;

import com.github.yizzuide.milkomeda.light.Cache;
import com.github.yizzuide.milkomeda.light.LightCache;
import com.github.yizzuide.milkomeda.light.LightCacheAspect;
import com.github.yizzuide.milkomeda.light.LightProperties;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * CrustConfig
 *
 * @author yizzuide
 * @since 1.14.0
 * @version 3.12.10
 * Create at 2019/11/11 14:56
 */
@Configuration
@ConditionalOnClass({AuthenticationManager.class})
@EnableConfigurationProperties({CrustProperties.class, LightProperties.class})
public class CrustConfig {

    @Autowired
    private LightProperties lightProps;

    @Autowired
    private CrustProperties crustProps;

    @Bean
    public Crust crust() {
        return new Crust();
    }

    @Autowired
    public void configCrustContext(Crust crust) {
        CrustContext.set(crust);
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = "milkomeda.crust", name = "use-bcrypt", havingValue = "true", matchIfMissing = true)
    public BCryptPasswordEncoder bCryptPasswordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = "milkomeda.crust", name = "enable-cache", havingValue = "true", matchIfMissing = true)
    public LightCacheAspect lightCacheAspect() {
        return new LightCacheAspect();
    }

    @Bean(Crust.CATCH_NAME)
    @ConditionalOnProperty(prefix = "milkomeda.crust", name = "enable-cache", havingValue = "true", matchIfMissing = true)
    public Cache lightCache() {
        LightCache lightCache = new LightCache();
        lightCache.setL1MaxCount(lightProps.getL1MaxCount());
        lightCache.setL1DiscardPercent(lightProps.getL1DiscardPercent());
        lightCache.setL1Expire(lightProps.getL1Expire().getSeconds());
        lightCache.setStrategy(lightProps.getStrategy());
        lightCache.setStrategyClass(lightProps.getStrategyClass());
        lightCache.setOnlyCacheL1(!crustProps.isEnableCacheL2());
        lightCache.setL2Expire(lightProps.getL2Expire().getSeconds());
        lightCache.setOnlyCacheL2(false);
        lightCache.setEnableSuperCache(lightProps.isEnableSuperCache());
        return lightCache;
    }

    @Configuration(proxyBeanMethods = false)
    @EnableConfigurationProperties(CrustProperties.class)
    public static class CrustURLMappingConfigurer implements WebMvcConfigurer {
        @Autowired
        private CrustProperties crustProps;

        public static final String staticLocation = "classpath:/static/";

        @Override
        public void addViewControllers(@NonNull ViewControllerRegistry registry) {
            if (StringUtils.isEmpty(crustProps.getRootRedirect())) {
                return;
            }
            // 添加根路径跳转
            registry.addRedirectViewController("/", crustProps.getRootRedirect());
            registry.setOrder(Ordered.HIGHEST_PRECEDENCE);
        }

        @Override
        public void addResourceHandlers(@NonNull ResourceHandlerRegistry registry) {
            if (StringUtils.isEmpty(crustProps.getStaticLocation())) {
                return;
            }
            // 设置静态资源，用于Spring Security配置
            registry.addResourceHandler("/**").addResourceLocations(crustProps.getStaticLocation());
        }
    }
}
