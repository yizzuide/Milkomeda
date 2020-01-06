package com.github.yizzuide.milkomeda.demo.moon;

import com.github.yizzuide.milkomeda.moon.Moon;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * MoonConfig
 *
 * @author yizzuide
 * Create at 2019/12/31 18:56
 */
@Configuration
public class MoonConfig {
    @Bean
    public Moon<String> mono() {
        Moon<String> moon = new Moon<>();
        moon.add("七牛云短信", "阿里云短信", "容联云短信");
        return moon;
    }
}
