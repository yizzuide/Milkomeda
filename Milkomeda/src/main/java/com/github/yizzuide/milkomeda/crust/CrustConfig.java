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
import org.springframework.aop.framework.autoproxy.AbstractAutoProxyCreator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Role;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * CrustConfig
 *
 * @see AbstractAutoProxyCreator#postProcessAfterInitialization(java.lang.Object, java.lang.String)
 * @author yizzuide
 * @since 1.14.0
 * @version 3.20.0
 * <br>
 * Create at 2019/11/11 14:56
 */
@Import(CrustURLMappingConfigurer.class)
@EnableConfigurationProperties({CrustProperties.class, LightProperties.class})
@AutoConfigureAfter(LightConfig.class)
@ConditionalOnClass({AuthenticationManager.class})
@Configuration
public class CrustConfig {

    @Autowired
    private CrustProperties crustProps;

    @Bean(AbstractCrust.BEAN_NAME)
    public AbstractCrust crust() {
        return new Crust();
    }

    @Bean
    @Role(BeanDefinition.ROLE_APPLICATION)
    public CrustTokenResolver crustTokenResolver() {
        return new CrustTokenResolver(crustProps);
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = "milkomeda.crust", name = "use-bcrypt", havingValue = "true", matchIfMissing = true)
    @Role(BeanDefinition.ROLE_APPLICATION)
    public BCryptPasswordEncoder bCryptPasswordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    @ConditionalOnMissingBean
    @Role(BeanDefinition.ROLE_INFRASTRUCTURE)
    @ConditionalOnProperty(prefix = "milkomeda.crust", name = "enable-cache", havingValue = "true", matchIfMissing = true)
    public LightCacheAspect lightCacheAspect() {
        return new LightCacheAspect();
    }

    @Bean(Crust.CATCH_NAME)
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = "milkomeda.crust", name = "enable-cache", havingValue = "true", matchIfMissing = true)
    @Role(BeanDefinition.ROLE_APPLICATION)
    public Cache crustLightCache() {
        LightCache lightCache = new LightCache();
        lightCache.setName(Crust.CATCH_NAME);
        lightCache.configFrom(crustProps.getCache());
        return lightCache;
    }

    @Bean(AbstractCrust.CODE_CATCH_NAME)
    @ConditionalOnProperty(prefix = "milkomeda.crust", name = "login-type", havingValue = "CODE")
    @Role(BeanDefinition.ROLE_APPLICATION)
    public Cache crustCodeLightCache() {
        LightCache lightCache = new LightCache();
        lightCache.setEnableSuperCache(false);
        lightCache.setOnlyCacheL2(true);
        lightCache.setL2Expire(crustProps.getCodeExpire().getSeconds());
        return lightCache;
    }

    @Bean
    @ConditionalOnMissingBean
    @Role(BeanDefinition.ROLE_INFRASTRUCTURE)
    public LightCacheCleanAstrolabeHandler lightCacheCleanAstrolabeHandler(@Autowired(required = false) LightThreadLocalScope scope) {
        return new LightCacheCleanAstrolabeHandler(scope);
    }
}
