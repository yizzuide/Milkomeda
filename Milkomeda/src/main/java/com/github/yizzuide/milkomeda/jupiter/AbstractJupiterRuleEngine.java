package com.github.yizzuide.milkomeda.jupiter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * AbstractJupiterRuleEngine
 *
 * @author yizzuide
 * @since 3.5.0
 * Create at 2020/05/20 10:54
 */
public abstract class AbstractJupiterRuleEngine implements JupiterRuleEngine {

    protected final Map<String, List<JupiterRuleItem>> ruleCacheMap = new HashMap<>();

    @Override
    public boolean run(String ruleName, List<JupiterRuleItem> ruleItemList) {
        addRule(ruleName, ruleItemList);
        return run(ruleName);
    }

    public void addRule(String ruleName, List<JupiterRuleItem> ruleItemList) {
        int incr = 0;
        for (JupiterRuleItem ruleItem : ruleItemList) {
            if (ruleItem.getId() == null) {
                ruleItem.setId(++incr);
            }
        }
        ruleCacheMap.put(ruleName, ruleItemList);
    }

    @Override
    public void resetRule(String ruleName, List<JupiterRuleItem> ruleItemList) {
        addRule(ruleName, ruleItemList);
    }
}
