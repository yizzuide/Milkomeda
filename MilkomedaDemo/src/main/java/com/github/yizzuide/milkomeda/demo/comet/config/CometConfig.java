package com.github.yizzuide.milkomeda.demo.comet.config;

import com.github.yizzuide.milkomeda.universe.metadata.BeanIds;
import com.github.yizzuide.milkomeda.universe.parser.url.URLPlaceholderResolver;
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

    // 自定义Logger解析，必需使用这个Bean名
    @Bean(BeanIds.COMET_LOGGER_RESOLVER)
    public URLPlaceholderResolver urlPlaceholderResolver() {
        return (placeholder, request) -> {
            if ("uid".equals(placeholder)) {
                String token = request.getHeader("token");
                // 假设以前四位为用户id
                return StringUtils.isBlank(token) ? null : token.substring(0, 4);
            }
            return null;
        };
    }

}
