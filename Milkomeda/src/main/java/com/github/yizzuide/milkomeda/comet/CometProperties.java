package com.github.yizzuide.milkomeda.comet;

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
    private UrlLog urlLog = new UrlLog();

    @Data
    public static class UrlLog {
        /**
         * 启用请求日志打印
         */
        private boolean enable = false;
        /**
         * 需要排除打印的路径
         */
        private List<String> exclude;
        /**
         * 打印策略
         * [{paths: ["/xxx/x"], tpl: "url->{userId}"}]
         */
        private List<Strategy> strategy;
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
