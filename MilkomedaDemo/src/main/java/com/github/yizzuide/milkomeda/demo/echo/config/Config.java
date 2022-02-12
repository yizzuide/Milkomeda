package com.github.yizzuide.milkomeda.demo.echo.config;

import com.github.yizzuide.milkomeda.demo.echo.props.ThirdKey;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Config
 *
 * @author yizzuide
 * Create at 2022/02/12 23:40
 */
@Configuration
@EnableConfigurationProperties(ThirdKey.class)
public class Config {
}
