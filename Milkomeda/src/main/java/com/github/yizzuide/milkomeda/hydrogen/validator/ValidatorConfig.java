package com.github.yizzuide.milkomeda.hydrogen.validator;

import com.github.yizzuide.milkomeda.hydrogen.core.HydrogenHolder;
import org.hibernate.validator.HibernateValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.validation.ValidationAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.beanvalidation.MethodValidationPostProcessor;

import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;

/**
 * ValidatorConfig
 * 参数效验器配置
 *
 * @author yizzuide
 * @since 3.0.0
 * Create at 2019/11/28 17:38
 */
@Configuration
@EnableConfigurationProperties(ValidatorProperties.class)
@AutoConfigureAfter(ValidationAutoConfiguration.class)
@ConditionalOnProperty(prefix = "milkomeda.hydrogen.validator", name = "enable", havingValue = "true")
public class ValidatorConfig {

    @Autowired
    public void config(ValidatorProperties validatorProperties) {
        ValidatorHolder.setProps(validatorProperties);
    }

    @Bean
    public MethodValidationPostProcessor methodValidationPostProcessor() {
        MethodValidationPostProcessor postProcessor = new MethodValidationPostProcessor();
        // 设置validator模式为快速失败返回
        postProcessor.setValidator(validator());
        return postProcessor;
    }

    @Bean
    public Validator validator() {
        ValidatorFactory validatorFactory = Validation.byProvider(HibernateValidator.class )
                .configure()
                // 设置validator模式为快速失败（只要有一个校验不通过就不立即返回错误）
                .failFast(true)
//                .addProperty( "hibernate.validator.fail_fast", "true" ) // 和上一个方法等同
                .buildValidatorFactory();
        Validator validator = validatorFactory.getValidator();
        HydrogenHolder.setValidator(validator);
        return validator;
    }
}
