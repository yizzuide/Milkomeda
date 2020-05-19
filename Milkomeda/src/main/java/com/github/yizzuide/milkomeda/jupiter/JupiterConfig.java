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
 * Create at 2020/05/19 16:57
 */
@EnableConfigurationProperties(JupiterProperties.class)
public class JupiterConfig implements ApplicationContextAware {

    @Autowired
    private JupiterProperties props;

    @Override
    public void setApplicationContext(@NonNull ApplicationContext applicationContext) throws BeansException {
        Map<String, JupiterProperties.RuleEngine> instances = props.getInstances();
        if (CollectionUtils.isEmpty(instances)) {
            return;
        }
        JupiterCompilerPool.put(JupiterCompilerType.EL.toString(), new JupiterElCompiler());
        for (Map.Entry<String, JupiterProperties.RuleEngine> ruleEngineEntry : instances.entrySet()) {
            String beanName = ruleEngineEntry.getKey();
            JupiterScopeRuleEngine jupiterRuleEngine = WebContext.registerBean((ConfigurableApplicationContext) applicationContext, beanName, JupiterScopeRuleEngine.class);
            applicationContext.getAutowireCapableBeanFactory().autowireBean(jupiterRuleEngine);

            List<JupiterRuleItem> jupiterRuleItemList = new ArrayList<>();
            for (Map.Entry<String, JupiterProperties.RuleItem> ruleItemEntry : ruleEngineEntry.getValue().getRuleItems().entrySet()) {
                JupiterProperties.RuleItem ruleItem = ruleItemEntry.getValue();
                jupiterRuleItemList.add(JupiterRuleItem.copyFrom(ruleItem));
            }
            jupiterRuleEngine.configRuleItems(jupiterRuleItemList);
        }
    }
}
