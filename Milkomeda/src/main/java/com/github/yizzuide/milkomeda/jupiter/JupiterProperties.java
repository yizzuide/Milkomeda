package com.github.yizzuide.milkomeda.jupiter;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Map;

/**
 * JupiterProperties
 *
 * @author yizzuide
 * @since 3.5.0
 * Create at 2020/05/19 22:13
 */
@Data
@ConfigurationProperties("milkomeda.jupiter")
public class JupiterProperties {

    /**
     * 规则引擎实例
     */
    private Map<String, RuleEngine> instances;

    /**
     * 规则引擎
     */
    @Data
    public static class RuleEngine {
        /**
         * 规则列表
         */
        private Map<String, RuleItem> ruleItems;
    }

    /**
     * 规则项
     */
    @Data
    public static class RuleItem {
        /**
         * 数据领域源（表名）
         */
        private String domain;

        /**
         * 源字段抽取
         */
        private String fields = "*";

        /**
         * 结果是否有多条记录
         */
        private boolean multi = true;

        /**
         * 过滤条件
         */
        private String filter;

        /**
         * 规则匹配表达式
         */
        private String match;

        /**
         * 匹配语法
         */
        private JupiterCompilerType syntax = JupiterCompilerType.EL;
    }
}
