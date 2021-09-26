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

import com.github.yizzuide.milkomeda.universe.context.WebContext;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.lang.NonNull;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * JupiterConfig
 *
 * @author yizzuide
 * @since 3.5.0
 * @version 3.5.1
 * Create at 2020/05/19 16:57
 */
@EnableConfigurationProperties(JupiterProperties.class)
public class JupiterConfig implements ApplicationContextAware {

    @Autowired
    private JupiterProperties props;

    @Override
    public void setApplicationContext(@NonNull ApplicationContext applicationContext) throws BeansException {
        JupiterCompilerPool.put(JupiterCompilerType.EL.toString(), new JupiterElCompiler());
        JupiterCompilerPool.put(JupiterCompilerType.OGNL.toString(), new JupiterOnglCompiler());
        Class<? extends JupiterRuleEngine> ruleEngineClass = null;
        if (props.getRuleEngineClazz() != null) {
            ruleEngineClass = props.getRuleEngineClazz();
        } else {
            if (props.getType() == JupiterRuleEngineType.SCOPE) {
                ruleEngineClass = JupiterScopeRuleEngine.class;
            }
        }
        JupiterRuleEngine jupiterRuleEngine = WebContext.registerBean((ConfigurableApplicationContext) applicationContext, JupiterRuleEngine.BEAN_ID, ruleEngineClass);
        applicationContext.getAutowireCapableBeanFactory().autowireBean(jupiterRuleEngine);

        Map<String, JupiterProperties.Rule> rules = props.getRules();
        if (CollectionUtils.isEmpty(rules)) {
            return;
        }
        for (Map.Entry<String, JupiterProperties.Rule> ruleEntry : rules.entrySet()) {
            List<JupiterRuleItem> jupiterRuleItemList = new ArrayList<>();
            for (Map.Entry<String, JupiterProperties.RuleItem> ruleItemEntry : ruleEntry.getValue().getRuleItems().entrySet()) {
                JupiterProperties.RuleItem ruleItem = ruleItemEntry.getValue();
                jupiterRuleItemList.add(JupiterRuleItem.copyFrom(ruleItem));
            }
            jupiterRuleEngine.addRule(ruleEntry.getKey(), jupiterRuleItemList);
        }
    }
}
