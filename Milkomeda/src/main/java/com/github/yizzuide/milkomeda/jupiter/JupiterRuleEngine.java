package com.github.yizzuide.milkomeda.jupiter;

import java.util.List;

/**
 * JupiterRuleEngine
 *
 * @author yizzuide
 * @since 3.5.0
 * @since 3.5.1
 * Create at 2020/05/19 14:35
 */
public interface JupiterRuleEngine {

    /**
     * 注册Bean名
     */
    String BEAN_ID = "jupiterRuleEngine";

    /**
     * 运行规则
     * @param ruleName  规则名
     * @return 是否通过，false拦截
     */
    boolean run(String ruleName);

    /**
     * 运行规则
     * @param ruleName      规则名
     * @param ruleItemList  规则列表
     * @return  是否通过，false拦截
     */
    boolean run(String ruleName, List<JupiterRuleItem> ruleItemList);

    /**
     * 添加规则
     * @param ruleName      规则名
     * @param ruleItemList  规则列表
     */
    void addRule(String ruleName, List<JupiterRuleItem> ruleItemList);

    /**
     * 重置规则
     * @param ruleName      规则名
     * @param ruleItemList  规则列表
     */
    void resetRule(String ruleName, List<JupiterRuleItem> ruleItemList);
}
