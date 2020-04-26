package com.github.yizzuide.milkomeda.hydrogen.core;

import com.github.yizzuide.milkomeda.hydrogen.filter.FilterConfig;
import com.github.yizzuide.milkomeda.hydrogen.i18n.I18nConfig;
import com.github.yizzuide.milkomeda.hydrogen.interceptor.InterceptorConfig;
import com.github.yizzuide.milkomeda.hydrogen.transaction.TransactionConfig;
import com.github.yizzuide.milkomeda.hydrogen.uniform.UniformConfig;
import com.github.yizzuide.milkomeda.hydrogen.validator.ValidatorConfig;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * HydrogenConfig
 *
 * @author yizzuide
 * @since 3.0.0
 * @version 3.2.0
 * Create at 2020/03/25 17:46
 */
@Configuration
@Import({
        TransactionConfig.class,
        UniformConfig.class,
        ValidatorConfig.class,
        I18nConfig.class,
        InterceptorConfig.class,
        FilterConfig.class
})
public class HydrogenConfig {
    // 在Spring Cloud环境下启用桥接事件监听
    @Bean
    @ConditionalOnClass(name = "org.springframework.cloud.context.environment.EnvironmentChangeEvent")
    public DelegatingEnvironmentChangeListener environmentChangeListener() {
        return new DelegatingEnvironmentChangeListener();
    }
}
