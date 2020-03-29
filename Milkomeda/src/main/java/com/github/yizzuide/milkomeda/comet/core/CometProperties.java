package com.github.yizzuide.milkomeda.comet.core;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

import java.util.List;

/**
 * CometProperties
 *
 * @author yizzuide
 * @since 2.0.0
 * @version 2.8.0
 * Create at 2019/12/12 18:04
 */
@Data
@ConfigurationProperties("milkomeda.comet")
public class CometProperties {
    /**
     * 允许读取请求消息体（使用@CometParam时必须开启）
     */
    private boolean enableReadRequestBody = true;

    /**
     * 请求日志
     */
    @NestedConfigurationProperty
    private CometProperties.Logger logger = new CometProperties.Logger();

    @Data
    public static class Logger {
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
         * [{paths: ["/xxx/x"], tpl: "url->{userId}"}]
         */
        private List<CometProperties.Strategy> strategy;
    }

    @Data
    public static class Strategy {
        /**
         * 策略包含路径
         */
        private List<String> paths;
        /**
         * 策略模板
         */
        private String tpl;
        /**
         * 缓存占位符
         */
        private List<String> cacheKeys;
    }
}
