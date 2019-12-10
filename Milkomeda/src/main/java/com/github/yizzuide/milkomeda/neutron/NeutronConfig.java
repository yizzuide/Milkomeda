package com.github.yizzuide.milkomeda.neutron;

import org.quartz.spi.JobFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.quartz.QuartzAutoConfiguration;
import org.springframework.boot.autoconfigure.quartz.QuartzProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;

/**
 * NeutronConfig
 *
 * @author yizzuide
 * @since 1.18.0
 * @version 1.18.1
 * Create at 2019/12/09 22:34
 */
@Import(QuartzAutoConfiguration.class)
@ConditionalOnClass(JobFactory.class)
@Configuration(proxyBeanMethods = false)
public class NeutronConfig {

    @Autowired
    private QuartzProperties props;

    @Autowired
    public void configHolder(SchedulerFactoryBean schedulerFactoryBean) {
        NeutronHolder.setScheduler(schedulerFactoryBean.getScheduler());
        NeutronHolder.setProps(props);
    }
}
