package com.github.yizzuide.milkomeda.demo.pulsar.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * WebMvcConfig
 *
 * @author yizzuide
 * Create at 2019/03/26 22:10
 */
@Slf4j
//@Configuration // SpringBoot 2.1.0默认已经提供装配了，下面的不需要配置了
public class WebMvcConfig implements WebMvcConfigurer {

    /*@Autowired
    private Pulsar pulsar;

    // 配置异步支持：超时、线程池等
    @Override
    public void configureAsyncSupport(AsyncSupportConfigurer configurer) {
        pulsar.configure(configurer, 5500);
    }*/
}
