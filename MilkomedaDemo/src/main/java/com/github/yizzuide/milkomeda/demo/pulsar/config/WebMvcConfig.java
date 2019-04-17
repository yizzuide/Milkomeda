package com.github.yizzuide.milkomeda.demo.pulsar.config;

import com.github.yizzuide.milkomeda.pulsar.Pulsar;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.AsyncSupportConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * WebMvcConfig
 *
 * @author yizzuide
 * Create at 2019/03/26 22:10
 */
@Slf4j
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Autowired
    private Pulsar pulsar;

    // 配置异步支持：超时、线程池等
    @Override
    public void configureAsyncSupport(AsyncSupportConfigurer configurer) {
        pulsar.configure(configurer, 1000);
    }
}
