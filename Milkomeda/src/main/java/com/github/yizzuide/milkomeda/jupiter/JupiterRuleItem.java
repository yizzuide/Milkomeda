package com.github.yizzuide.milkomeda.jupiter;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * PayRuleItem
 * 规则匹配配置项
 *
 * @author yizzuide
 * @since 3.5.0
 * Create at 2020/05/18 18:02
 */
@NoArgsConstructor
@Data
public class JupiterRuleItem implements Serializable {
    private static final long serialVersionUID = -368706902995447841L;
    /**
     * 全局唯一id（默认会自增长）
     */
    private Integer id;

    /**
     * 数据领域源
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
    private String syntax = JupiterCompilerType.EL.toString();

    public JupiterRuleItem(String domain, String fields, String filter, boolean multi, String match) {
        this.domain = domain;
        this.fields = fields;
        this.filter = filter;
        this.multi = multi;
        this.match = match;
    }

    /**
     * 从配置拷贝规则项
     * @param ruleItem  JupiterProperties.RuleItem
     * @return  JupiterRuleItem
     */
    static JupiterRuleItem copyFrom(JupiterProperties.RuleItem ruleItem) {
        JupiterRuleItem jupiterRuleItem = new JupiterRuleItem();
        jupiterRuleItem.setDomain(ruleItem.getDomain());
        jupiterRuleItem.setFields(ruleItem.getFields());
        jupiterRuleItem.setMulti(ruleItem.isMulti());
        jupiterRuleItem.setFilter(ruleItem.getFilter());
        jupiterRuleItem.setMatch(ruleItem.getMatch());
        jupiterRuleItem.setSyntax(ruleItem.getSyntax().toString());
        return jupiterRuleItem;
    }
}
