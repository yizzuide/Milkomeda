package com.github.yizzuide.milkomeda.crust;

import com.github.yizzuide.milkomeda.light.Cache;
import com.github.yizzuide.milkomeda.light.LightCache;
import com.github.yizzuide.milkomeda.light.LightProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * CrustConfig
 *
 * @author yizzuide
 * @since 1.14.0
 * @version 1.16.4
 * Create at 2019/11/11 14:56
 */
@Configuration
@ConditionalOnClass({AuthenticationManager.class})
@EnableConfigurationProperties({CrustProperties.class, LightProperties.class})
public class CrustConfig {

    @Autowired
    private LightProperties lightProps;

    @Autowired
    private CrustProperties crustProps;

    @Bean
    public Crust crust() {
        return new Crust();
    }

    @Autowired
    public void configCrustContext(Crust crust) {
        CrustContext.set(crust);
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = "milkomeda.crust", name = "use-bcrypt", havingValue = "true", matchIfMissing = true)
    public BCryptPasswordEncoder bCryptPasswordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean(Crust.CATCH_NAME)
    @ConditionalOnProperty(prefix = "milkomeda.crust", name = "enable-cache", havingValue = "true", matchIfMissing = true)
    public Cache lightCache() {
        LightCache lightCache = new LightCache();
        lightCache.setL1MaxCount(lightProps.getL1MaxCount());
        lightCache.setL1DiscardPercent(lightProps.getL1DiscardPercent());
        lightCache.setStrategy(lightProps.getStrategy());
        lightCache.setStrategyClass(lightProps.getStrategyClass());
        lightCache.setOnlyCacheL1(!crustProps.isEnableCacheL2());
        lightCache.setL2Expire(lightProps.getL2Expire());
        return lightCache;
    }

}
