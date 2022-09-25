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

package com.github.yizzuide.milkomeda.ice.inspector;

import com.github.yizzuide.milkomeda.ice.IceHolder;
import com.github.yizzuide.milkomeda.ice.IceProperties;
import org.apache.ibatis.annotations.Mapper;
import org.jetbrains.annotations.NotNull;
import org.mybatis.spring.mapper.MapperScannerConfigurer;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.env.Environment;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.util.ClassUtils;
import org.springframework.util.StringUtils;

import java.beans.PropertyDescriptor;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Job introspection config.
 *
 * @author yizzuide
 * @since 3.14.0
 * Create at 2022/09/25 17:37
 */
@Configuration
@EnableConfigurationProperties(IceProperties.class)
@Import(IntrospectConfig.IntrospectRegistrar.class)
@ConditionalOnProperty(prefix = "milkomeda.ice.introspect", name = "enable", havingValue = "true")
public class IntrospectConfig {

    @Bean
    public JobInspector jobInspector(IceProperties iceProperties) {
        JobInspector.InspectorType inspectorType = iceProperties.getIntrospect().getInspectorType();
        JobInspector jobInspector = null;
        switch (inspectorType) {
            case REDIS:
                jobInspector = new RedisJobInspector(iceProperties);
                break;
            case MYSQL:
                jobInspector = new MysqlJobInspector();
                break;
            case MONGODB:
                break;
            case AUTO_CHECK:
                break;
        }
        IceHolder.setJobInspector(jobInspector);
        return jobInspector;
    }

    public static class IntrospectRegistrar implements ImportBeanDefinitionRegistrar {

        private final IceProperties iceProperties;

        public IntrospectRegistrar(Environment environment) {
            this.iceProperties = Binder.get(environment).bind(IceProperties.PREFIX, IceProperties.class).get();
        }

        @Override
        public void registerBeanDefinitions(@NotNull AnnotationMetadata importingClassMetadata, @NotNull BeanDefinitionRegistry registry) {
            JobInspector.InspectorType inspectorType = iceProperties.getIntrospect().getInspectorType();
            if (inspectorType != JobInspector.InspectorType.MYSQL) {
                return;
            }

            List<String> packages = new ArrayList<>();
            packages.add(ClassUtils.getPackageName(IntrospectConfig.class) + ".mapper");

            BeanDefinitionBuilder builder = BeanDefinitionBuilder.genericBeanDefinition(MapperScannerConfigurer.class);
            builder.addPropertyValue("processPropertyPlaceHolders", true);
            builder.addPropertyValue("annotationClass", Mapper.class);
            builder.addPropertyValue("basePackage", StringUtils.collectionToCommaDelimitedString(packages));
            BeanWrapper beanWrapper = new BeanWrapperImpl(MapperScannerConfigurer.class);
            Set<String> propertyNames = Stream.of(beanWrapper.getPropertyDescriptors()).map(PropertyDescriptor::getName)
                    .collect(Collectors.toSet());
            if (propertyNames.contains("lazyInitialization")) {
                // Need to mybatis-spring 2.0.2+
                builder.addPropertyValue("lazyInitialization", "${mybatis.lazy-initialization:false}");
            }
            if (propertyNames.contains("defaultScope")) {
                // Need to mybatis-spring 2.0.6+
                builder.addPropertyValue("defaultScope", "${mybatis.mapper-default-scope:}");
            }
            registry.registerBeanDefinition(MapperScannerConfigurer.class.getName() + "#jobInterospector", builder.getBeanDefinition());
        }
    }
}
