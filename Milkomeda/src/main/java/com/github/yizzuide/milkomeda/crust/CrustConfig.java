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

import com.github.yizzuide.milkomeda.light.*;
import org.apache.commons.lang3.StringUtils;
import org.springframework.aop.framework.autoproxy.AbstractAutoProxyCreator;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
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
import org.springframework.util.CollectionUtils;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import javax.annotation.Resource;

/**
 * CrustConfig
 *
 * @see AbstractAutoProxyCreator#postProcessAfterInitialization(java.lang.Object, java.lang.String)
 * @author yizzuide
 * @since 1.14.0
 * @version 3.14.0
 * <br>
 * Create at 2019/11/11 14:56
 */
@ConditionalOnClass({AuthenticationManager.class})
@EnableConfigurationProperties({CrustProperties.class, LightProperties.class})
@AutoConfigureAfter(LightConfig.class)
@Configuration
public class CrustConfig {

    @Autowired
    private CrustProperties crustProps;

    @Bean
    public Crust crust() {
        return new Crust();
    }

    @Bean
    public CrustTokenResolver crustTokenResolver() {
        return new CrustTokenResolver(crustProps);
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
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = "milkomeda.crust", name = "enable-cache", havingValue = "true", matchIfMissing = true)
    public Cache crustLightCache(LightProperties lightProps) {
        LightCache lightCache = new LightCache();
        lightCache.setL1MaxCount(lightProps.getL1MaxCount());
        lightCache.setL1DiscardPercent(lightProps.getL1DiscardPercent());
        lightCache.setL1Expire(crustProps.getExpire().getSeconds());
        lightCache.setStrategy(LightDiscardStrategy.LazyExpire);
        lightCache.setOnlyCacheL1(crustProps.isCacheInMemory());
        lightCache.setL2Expire(crustProps.getExpire().getSeconds());
        lightCache.setEnableSuperCache(lightProps.isEnableSuperCache());
        return lightCache;
    }

    @Bean(Crust.CODE_CATCH_NAME)
    @ConditionalOnProperty(prefix = "milkomeda.crust", name = "login-type", havingValue = "CODE")
    public Cache crustCodeLightCache() {
        LightCache lightCache = new LightCache();
        lightCache.setEnableSuperCache(false);
        lightCache.setOnlyCacheL2(true);
        lightCache.setL2Expire(crustProps.getCodeExpire().getSeconds());
        return lightCache;
    }

    @Bean
    @ConditionalOnMissingBean
    public LightCacheCleanAstrolabeHandler lightCacheCleanAstrolabeHandler(@Autowired(required = false) LightThreadLocalScope scope) {
        return new LightCacheCleanAstrolabeHandler(scope);
    }

    @Configuration(proxyBeanMethods = false)
    @EnableConfigurationProperties(CrustProperties.class)
    public static class CrustURLMappingConfigurer implements WebMvcConfigurer, InitializingBean {
        @Autowired
        private CrustProperties crustProps;

        @Resource
        private Crust crust;

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
            if (!StringUtils.isEmpty(crustProps.getStaticLocation())) {
                // 设置静态资源，用于Spring Security配置
                registry.addResourceHandler("/**").addResourceLocations(crustProps.getStaticLocation());
            }

            if (!CollectionUtils.isEmpty(crustProps.getResourceMappings())) {
                crustProps.getResourceMappings().forEach(ResourceMapping ->
                        registry.addResourceHandler(ResourceMapping.getPathPatterns().toArray(new String[0]))
                                .addResourceLocations(ResourceMapping.getTargetLocations().toArray(new String[0])));
            }
        }

        @Override
        public void afterPropertiesSet() throws Exception {
            CrustContext.set(crust);
        }
    }
}
