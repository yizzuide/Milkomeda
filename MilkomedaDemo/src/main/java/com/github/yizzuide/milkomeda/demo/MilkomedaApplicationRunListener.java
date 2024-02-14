package com.github.yizzuide.milkomeda.demo;

import com.github.yizzuide.milkomeda.ice.IceHolder;
import com.github.yizzuide.milkomeda.jupiter.JupiterRuleEngine;
import com.github.yizzuide.milkomeda.universe.context.ApplicationContextHolder;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplicationRunListener;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Lazy;

import java.time.Duration;

/**
 * MilkomedaApplicationRunListener
 *
 * @author yizzuide
 * <br>
 * Create at 2020/04/16 18:24
 */
// SpringApplicationRunListener包含了Spring所有启动事件的监听器
@Slf4j
public class MilkomedaApplicationRunListener implements SpringApplicationRunListener {
    // yml配置的实体需要使用@Lazy!!!
    @Lazy
    @Resource
    private JupiterRuleEngine jupiterRuleEngine;

    @Override
    public void started(ConfigurableApplicationContext context, Duration timeTaken) {
        // 设置延迟队列实例名
        //IceHolder.setInstanceName("milkomeda-demo");
        // 激活Dead queue里的job（重试超过次数的job）
        IceHolder.activeDeadJobs();

        // 添加自定义环境变量到milkomedaProperties，给Spring EL表达 #env 提供数据源，如：#env.product
        ApplicationContextHolder.getEnvironment().put("product",  "milkomeda");
        // 可以从Spring ConfigurableEnvironment中获取
        log.info("#env - {}", ApplicationContextHolder.getEnvironment().get("product"));

        // 从json配置加载覆盖yml配置
        //String ruleItems = "[{\"match\":\"true\"},{\"domain\":\"t_order\",\"fields\":\"id\",\"filter\":\"user_id={$attr.userInfo.uid}\",\"match\":\"size()==0\",\"syntax\":\"el\"}]";
        //jupiterRuleEngine.resetRule("payRule", JSONUtil.parseList(ruleItems, JupiterRuleItem.class));
    }
}
