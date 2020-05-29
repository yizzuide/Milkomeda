package com.github.yizzuide.milkomeda.moon;

import com.github.yizzuide.milkomeda.atom.AtomConfig;
import com.github.yizzuide.milkomeda.light.LightConfig;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Import;

/**
 * MixinMoonConfig
 *
 * @author yizzuide
 * @since 3.7.0
 * Create at 2020/05/29 14:10
 */
@Import({LightConfig.class, AtomConfig.class})
@ConditionalOnProperty(prefix = "milkomeda.moon", name = "mixin-mode", havingValue = "true")
public class MixinMoonConfig {
}
