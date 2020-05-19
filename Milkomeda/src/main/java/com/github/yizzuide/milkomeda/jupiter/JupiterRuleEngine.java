package com.github.yizzuide.milkomeda.jupiter;

import java.util.List;

/**
 * JupiterRuleEngine
 *
 * @author yizzuide
 * @since 3.5.0
 * Create at 2020/05/19 14:35
 */
public interface JupiterRuleEngine {

    /**
     * 运行规则
     * @return 是否通过，false拦截
     */
    boolean run();

    /**
     * 配置规则列表
     * @param ruleItemList  规则列表
     */
    void configRuleItems(List<JupiterRuleItem> ruleItemList);
}
