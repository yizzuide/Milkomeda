package com.github.yizzuide.milkomeda.comet.collector;

import com.github.yizzuide.milkomeda.comet.core.WebCometData;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * CometCollectorProperties
 *
 * @author yizzuide
 * @since 2.8.0
 * Create at 2020/03/28 21:28
 */
@Data
@ConfigurationProperties("milkomeda.comet.collector")
public class CometCollectorProperties {
    /**
     * 开启URL标签日志收集
     */
    private boolean enableTag = false;

    /**
     * 标签集合
     */
    private Map<String, Tag> tags = new HashMap<>();

    @Data
    public static class Tag {
        /**
         * CometData原型类
         */
        private Class<? extends WebCometData> prototype = WebCometData.class;

        /**
         * 失败条件匹配
         */
        private Map<String, Object> failureCondition;

        /**
         * 匹配的请求路径
         */
        private List<String> paths;
    }
}
