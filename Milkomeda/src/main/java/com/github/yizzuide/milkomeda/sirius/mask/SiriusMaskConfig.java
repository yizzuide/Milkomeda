/*
 * Copyright (c) 2025 yizzuide All rights Reserved.
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

package com.github.yizzuide.milkomeda.sirius.mask;

import com.fasterxml.jackson.databind.AnnotationIntrospector;
import com.fasterxml.jackson.databind.introspect.AnnotationIntrospectorPair;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.ContextRefreshedEvent;

import java.util.List;

/**
 * Sensitive field masking config.
 *
 * @version 4.0.0
 * @author yizzuide
 * Create at 2025/07/24 13:42
 */
@ConditionalOnProperty(prefix = SiriusMaskProperties.PREFIX, name = "enable", havingValue = "true")
@EnableConfigurationProperties(SiriusMaskProperties.class)
@Configuration(proxyBeanMethods = false)
public class SiriusMaskConfig implements ApplicationListener<ContextRefreshedEvent> {

    @Autowired
    private SiriusMaskProperties maskProperties;

    @Autowired(required = false)
    private List<MaskFilter> maskFilters;

    @Bean
    public SiriusMaskInterceptor siriusMaskInterceptor() {
        return new SiriusMaskInterceptor(maskProperties, maskFilters);
    }

    @Bean
    public Jackson2ObjectMapperBuilderCustomizer maskingObjectMapperCustomizer() {
        return (builder) -> builder.postConfigurer((objectMapper) -> {
            AnnotationIntrospector annoIntro = objectMapper.getSerializationConfig().getAnnotationIntrospector();
            AnnotationIntrospector maskAnnoIntro = AnnotationIntrospectorPair.pair(annoIntro, new SiriusMaskAnnotationIntrospector(maskProperties, maskFilters));
            objectMapper.setAnnotationIntrospector(maskAnnoIntro);
        });
    }

    @Override
    public void onApplicationEvent(@NotNull ContextRefreshedEvent event) {
        List<String> getterMethods = maskProperties.getMaskFields().stream().map(field ->
                "get" + Character.toUpperCase(field.charAt(0)) + field.substring(1)).toList();
        maskProperties.setMaskFields(getterMethods);
    }
}
