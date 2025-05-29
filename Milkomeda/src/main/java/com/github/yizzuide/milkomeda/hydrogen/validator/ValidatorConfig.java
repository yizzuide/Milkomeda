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

package com.github.yizzuide.milkomeda.hydrogen.validator;

import com.github.yizzuide.milkomeda.hydrogen.core.HydrogenHolder;
import com.github.yizzuide.milkomeda.universe.context.ApplicationContextHolder;
import jakarta.annotation.Nonnull;
import jakarta.validation.Validator;
import jakarta.validation.valueextraction.ValueExtractor;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.validation.ValidationAutoConfiguration;
import org.springframework.boot.autoconfigure.validation.ValidationConfigurationCustomizer;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

import java.util.List;

/**
 * ValidatorConfig
 * 参数效验器配置
 *
 * @author yizzuide
 * @since 3.0.0
 * @version 4.0.0
 * <br>
 * Create at 2019/11/28 17:38
 */
@Configuration
@EnableConfigurationProperties(ValidatorProperties.class)
@AutoConfigureAfter(ValidationAutoConfiguration.class)
@ConditionalOnProperty(prefix = "milkomeda.hydrogen.validator", name = "enable", havingValue = "true")
public class ValidatorConfig implements InitializingBean, ApplicationListener<ApplicationStartedEvent> {

    @Lazy
    @Autowired
    private ValidatorProperties validatorProperties;

    @Bean
    public ValueExtractor<?> resultValueExtractor() {
        return new ResultValueExtractor();
    }

    @Bean
    public ValidationConfigurationCustomizer validCustomizer(List<ValueExtractor<?>> valueExtractors) {
        return configuration -> {
            // 设置为快速失效
            configuration.addProperty("hibernate.validator.fail_fast", "true");
            if (valueExtractors != null) {
                valueExtractors.forEach(configuration::addValueExtractor);
            }
        };
    }

    // Spring Boot 3.0: `LocalValidatorFactoryBean` relies on standard parameter name resolution in Bean Validation 3.0 now,
    //  just configuring additional Kotlin reflection if Kotlin is present. If you refer to parameter names in your Bean Validation setup,
    //  make sure to compile your Java sources with the Java 8+ `-parameters` flag.

    // 以下为自定义实现，现替换为Spring Boot配置ValidationAutoConfiguration下的`LocalValidatorFactoryBean`
    /*@Role(BeanDefinition.ROLE_INFRASTRUCTURE)
    @Bean
    public static MethodValidationPostProcessor methodValidationPostProcessor() {
        MethodValidationPostProcessor postProcessor = new MethodValidationPostProcessor();
        // 设置validator模式为快速失败返回
        postProcessor.setValidator(validator());
        return postProcessor;
    }

    @Role(BeanDefinition.ROLE_INFRASTRUCTURE)
    @Bean
    public static Validator validator() {
        @Cleanup
        ValidatorFactory validatorFactory = Validation.byProvider(HibernateValidator.class)
                .configure()
                // 设置validator模式为快速失败（只要有一个校验不通过就不立即返回错误）
                .failFast(true)
                //.addProperty("hibernate.validator.fail_fast", "true") // 和上一个方法等同
                .buildValidatorFactory();
        Validator validator = validatorFactory.getValidator();
        HydrogenHolder.setValidator(validator);
        return validator;
    }*/

    @Override
    public void afterPropertiesSet() throws Exception {
        ValidatorHolder.setProps(validatorProperties);
    }

    @Override
    public void onApplicationEvent(@Nonnull ApplicationStartedEvent event) {
        HydrogenHolder.setValidator(ApplicationContextHolder.get().getBean(Validator.class));
    }
}
