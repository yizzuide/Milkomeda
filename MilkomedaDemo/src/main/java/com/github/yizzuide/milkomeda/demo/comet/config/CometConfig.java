package com.github.yizzuide.milkomeda.demo.comet.config;

import com.github.yizzuide.milkomeda.comet.logger.CometLoggerResolver;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * CometConfig
 *
 * @author yizzuide
 * Create at 2019/04/11 22:18
 */
@Slf4j
@Configuration
public class CometConfig {

    // UrlLog解析uid占位符
    @Bean
    public CometLoggerResolver cometUrlLogResolver() {
        return (k, request) -> {
            if ("uid".equals(k)) {
                String token = request.getHeader("token");
                // 假设以前四位为用户id
                return StringUtils.isBlank(token) ? null : token.substring(0, 4);
            }
            return null;
        };
    }

}
