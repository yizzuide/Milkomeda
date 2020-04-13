package com.github.yizzuide.milkomeda.comet.logger;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * CometLoggerProperties
 *
 * @author yizzuide
 * @since 3.0.0
 * @since 3.0.3
 * Create at 2020/04/05 18:47
 */
@Data
@ConfigurationProperties("milkomeda.comet.logger")
public class CometLoggerProperties {
    /**
     * 启用请求日志打印
     */
    private boolean enable = false;
    /**
     * 占位符前缀
     */
    private String prefix = "{";
    /**
     * 占位符后缀
     */
    private String suffix = "}";
    /**
     * 需要排除打印的路径
     */
    private List<String> exclude;
    /**
     * 打印策略
     */
    private List<Strategy> strategy = Collections.singletonList(new Strategy());

    @Data
    public static class Strategy {
        /**
         * 策略包含路径
         */
        private List<String> paths = Collections.singletonList("/**");
        /**
         * 策略模板，默认能识别的占位符：uri、method、params（参数可配置占位符，请求头固定使用[]）
         */
        private String tpl = "{\"uri\":\"{uri}\", \"method\": \"{method}\", \"params\": \"{params}\", \"token\": \"(token)\"}";
        /**
         * 缓存占位符（模块内部使用）
         */
        private Map<String, List<String>> cacheKeys;
    }
}
