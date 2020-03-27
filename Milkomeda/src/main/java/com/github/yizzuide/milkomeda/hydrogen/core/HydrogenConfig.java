package com.github.yizzuide.milkomeda.hydrogen.core;

import com.github.yizzuide.milkomeda.hydrogen.i18n.I18nConfig;
import com.github.yizzuide.milkomeda.hydrogen.transaction.TransactionAdviceConfig;
import com.github.yizzuide.milkomeda.hydrogen.uniform.UniformExceptionResponseConfig;
import com.github.yizzuide.milkomeda.hydrogen.validator.ValidatorConfig;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * HydrogenConfig
 *
 * @author yizzuide
 * @since 2.8.0
 * Create at 2020/03/25 17:46
 */
@Configuration
@Import({TransactionAdviceConfig.class, UniformExceptionResponseConfig.class, ValidatorConfig.class, I18nConfig.class})
public class HydrogenConfig {
}
