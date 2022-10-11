/*
 * Copyright (c) 2021 yizzuide All rights Reserved.
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.github.yizzuide.milkomeda.jupiter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * AbstractJupiterRuleEngine
 *
 * @author yizzuide
 * @since 3.5.0
 * @version 3.5.1
 * <br>
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
            ruleItem.setSyntax(ruleItem.getSyntax().toUpperCase());
        }
        ruleCacheMap.put(ruleName, ruleItemList);
    }

    @Override
    public void resetRule(String ruleName, List<JupiterRuleItem> ruleItemList) {
        addRule(ruleName, ruleItemList);
    }
}
