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

package com.github.yizzuide.milkomeda.crust;

import com.github.yizzuide.milkomeda.util.IdGenerator;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.lang.NonNull;
import org.springframework.util.CollectionUtils;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import javax.annotation.Resource;

/**
 * URL mapping configurer extends {@link WebMvcConfigurer}.
 * @author yizzuide
 * @since 1.14.0
 * @version 3.20.0
 * <br>
 * Create at 2024/01/12 14:56
 */
@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(CrustProperties.class)
public class CrustURLMappingConfigurer implements WebMvcConfigurer, InitializingBean {

    @Autowired
    private CrustProperties crustProps;

    @Resource
    private AbstractCrust crust;

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
        // Set an AES key with 256 bit
        if (!crustProps.isUseRsa() && crustProps.getSecureKey() == null) {
            crustProps.setSecureKey(IdGenerator.genRand(8) + "_" + IdGenerator.genRand(6));
        }
        CrustContext.set(crust);
    }
}