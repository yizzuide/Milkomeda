package com.github.yizzuide.milkomeda.demo;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;

/**
 * MilkomedaDemoInitializer
 * War包启动引导
 *
 * @author yizzuide
 * Create at 2019/11/21 18:18
 */
@Slf4j
public class MilkomedaDemoInitializer extends SpringBootServletInitializer {
    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder builder) {
        return builder.sources(MilkomedaDemoApplication.class);
    }
}
