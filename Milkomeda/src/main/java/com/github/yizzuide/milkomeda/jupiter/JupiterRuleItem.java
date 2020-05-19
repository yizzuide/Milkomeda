package com.github.yizzuide.milkomeda.jupiter;

import lombok.Data;

import java.io.Serializable;

/**
 * PayRuleItem
 * 规则匹配配置项
 *
 * @author yizzuide
 * @since 3.5.0
 * Create at 2020/05/18 18:02
 */
@Data
public class JupiterRuleItem implements Serializable {
    private static final long serialVersionUID = -368706902995447841L;
    /**
     * 数据源
     */
    private String domain;

    /**
     * 源字段抽取
     */
    private String fields = "*";
    /**
     * 源是否是多个
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
    private String syntax = JupiterRuleEngine.DEFAULT_SYNTAX;

    public JupiterRuleItem() {}

    public JupiterRuleItem(String domain, String fields, String filter, String match) {
        this.domain = domain;
        this.fields = fields;
        this.filter = filter;
        this.match = match;
    }
}
