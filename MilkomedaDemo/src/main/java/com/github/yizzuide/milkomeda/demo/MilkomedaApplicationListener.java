package com.github.yizzuide.milkomeda.demo;

import com.github.yizzuide.milkomeda.ice.IceHolder;
import com.github.yizzuide.milkomeda.universe.context.ApplicationContextHolder;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

/**
 * MilkomedaApplicationListener
 *
 * @author yizzuide
 * Create at 2020/04/16 18:24
 */
@Component
public class MilkomedaApplicationListener implements ApplicationListener<ContextRefreshedEvent> {
    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        // 设置延迟队列实例名
        IceHolder.setInstanceName("product");
        // 激活Dead queue里的job（重试超过次数的job）
        IceHolder.activeDeadJobs();

        // 调用环境变量，给Spring EL表达 #env 提供数据源
        ApplicationContextHolder.getEnvironment().put("product",  "milkomeda");
    }
}
