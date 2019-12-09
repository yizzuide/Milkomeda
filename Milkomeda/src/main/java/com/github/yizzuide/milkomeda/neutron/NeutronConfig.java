package com.github.yizzuide.milkomeda.neutron;

import com.github.yizzuide.milkomeda.universe.context.ApplicationContextHolder;
import org.apache.commons.lang3.StringUtils;
import org.quartz.spi.JobFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;
import org.springframework.scheduling.quartz.SpringBeanJobFactory;

import javax.sql.DataSource;
import java.util.Properties;

/**
 * NeutronConfig
 *
 * @author yizzuide
 * @since 1.18.0
 * Create at 2019/12/09 22:34
 */
@EnableConfigurationProperties(NeutronProperties.class)
@ConditionalOnClass(JobFactory.class)
@Configuration
public class NeutronConfig {

    @Autowired
    private NeutronProperties props;

    @Bean
    public JobFactory jobFactory(ApplicationContext applicationContext) {
        // 创建基于自动注入的SpringBeanJobFactory，Job通过Spring Bean方式获取（Job类有@Component注解）或创建
        SpringBeanJobFactory jobFactory = new SpringBeanJobFactory();
        jobFactory.setApplicationContext(applicationContext);
        return jobFactory;
    }

    @Bean
    public SchedulerFactoryBean schedulerFactoryBean(JobFactory jobFactory) {
        // 创建调度器Bean，默认使用StdSchedulerFactory
        SchedulerFactoryBean factory = new SchedulerFactoryBean();
        factory.setQuartzProperties(quartzProperties());
        // 启动时更新已存在的Job
        factory.setOverwriteExistingJobs(true);
        factory.setAutoStartup(true);
        // 持久化数据源
        if (props.isSerializable()) {
            DataSource dataSource = ApplicationContextHolder.get().getBean(DataSource.class);
            factory.setDataSource(dataSource);
        }
        factory.setJobFactory(jobFactory);
        return factory;
    }

    @Bean
    public Properties quartzProperties() {
        Properties properties = new Properties();
        properties.setProperty("org.quartz.scheduler.instanceName", props.getScheduler().getInstanceName());
        properties.setProperty("org.quartz.scheduler.instanceId", props.getScheduler().getInstanceId());
        properties.setProperty("org.quartz.scheduler.makeSchedulerThreadDaemon", String.valueOf(props.getScheduler().isMakeSchedulerThreadDaemon()));

        properties.setProperty("org.quartz.threadPool.class", props.getThreadPool().getThreadPoolClass());
        properties.setProperty("org.quartz.threadPool.makeThreadsDaemons", String.valueOf(props.getThreadPool().isMakeThreadsDaemons()));
        properties.setProperty("org.quartz.threadPool.threadCount", String.valueOf(props.getThreadPool().getThreadCount()));
        properties.setProperty("org.quartz.threadPool.threadPriority", String.valueOf(props.getThreadPool().getThreadPriority()));
        properties.setProperty("org.quartz.threadPool.threadsInheritContextClassLoaderOfInitializingThread", String.valueOf(props.getThreadPool().isThreadsInheritContextClassLoaderOfInitializingThread()));

        if (props.isSerializable()) {
            properties.setProperty("org.quartz.jobStore.class", props.getJobStore().getJobStoreClass());
            properties.setProperty("org.quartz.jobStore.driverDelegateClass", props.getJobStore().getDriverDelegateClass());
            properties.setProperty("org.quartz.jobStore.tablePrefix", props.getJobStore().getTablePrefix());
            properties.setProperty("org.quartz.jobStore.isClustered", String.valueOf(props.getJobStore().isClustered()));
            if (StringUtils.isNotBlank(props.getJobStore().getDataSource())) {
                properties.setProperty("org.quartz.jobStore.dataSource", props.getJobStore().getDataSource());
            }
            properties.setProperty("org.quartz.jobStore.misfireThreshold", String.valueOf(props.getJobStore().getMisfireThreshold()));
            properties.setProperty("org.quartz.jobStore.clusterCheckinInterval", String.valueOf(props.getJobStore().getClusterCheckinInterval()));
        }
        return properties;
    }

    @Autowired
    public void configHolder(SchedulerFactoryBean schedulerFactoryBean) {
        NeutronHolder.setScheduler(schedulerFactoryBean.getScheduler());
        NeutronHolder.setProps(props);
    }
}
