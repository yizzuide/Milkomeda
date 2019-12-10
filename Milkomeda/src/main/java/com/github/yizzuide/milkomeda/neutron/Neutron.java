package com.github.yizzuide.milkomeda.neutron;

import lombok.extern.slf4j.Slf4j;
import org.quartz.*;
import org.springframework.boot.autoconfigure.quartz.JobStoreType;
import org.springframework.scheduling.quartz.CronTriggerFactoryBean;

import java.text.ParseException;

/**
 * Neutron
 *
 * @author yizzuide
 * @since 1.18.0
 * @version 1.18.1
 * Create at 2019/12/09 22:39
 */
@Slf4j
public class Neutron {
    private static String JOB_GROUP_NAME = "NEUTRON_JOB_GROUP_NAME";
    private static String TRIGGER_GROUP_NAME = "NEUTRON_TRIGGER_GROUP_NAME";

    /**
     * 验证cron表达式
     * @param cron  cron表达式
     * @return true为正确
     */
    public boolean verifyCron(String cron) {
        try {
            CronScheduleBuilder.cronSchedule(cron);
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    /**
     * 创建JOB
     * @param jobName   job名称
     * @param group     job组
     * @param jobClass  job类
     * @return  jobDetail
     */
    public static JobDetail createJobDetail(String jobName, String group, Class<? extends Job> jobClass/*, String description*/) {
        return createJobDetail(jobName, group, jobClass, "");
    }

    /**
     * 创建JOB
     * @param jobName   job名称
     * @param group     job组
     * @param jobClass  job类
     * @return  jobDetail
     */
    public static JobDetail createJobDetail(String jobName, String group, Class<? extends Job> jobClass, String description) {
        return JobBuilder.newJob(jobClass)
                .withIdentity(jobName, group)
                 .withDescription(description)
                // 持久化到数据库
                 .storeDurably(NeutronHolder.getProps().getJobStoreType() == JobStoreType.JDBC)
                .build();
    }

    /**
     * cron表达式触发器
     * @param triggerName 触发器名称
     * @param triggerGroup 触发器所在组
     * @param cronExpression cron表达式
     * @return CronTrigger
     */
    public static CronTrigger createCronTrigger(String triggerName, String triggerGroup, String cronExpression) throws ParseException {
        CronTriggerFactoryBean factoryBean = new CronTriggerFactoryBean();
        factoryBean.setName(triggerName);
        factoryBean.setGroup(triggerGroup);
        factoryBean.setCronExpression(cronExpression);
        // 由于是手动创建的Bean，需要调用afterPropertiesSet()属性初始化方法
        factoryBean.afterPropertiesSet();
        return factoryBean.getObject();
    }

    /**
     * 添加一个定时任务，使用默认的任务组名，触发器名，触发器组名
     *
     * @param jobName   job name
     * @param clazz     job clazz
     * @param cron      cron表达式
     */
    public static void addJob(String jobName, Class<? extends Job> clazz, String cron) {
        try {
            Scheduler scheduler = NeutronHolder.getScheduler();
            JobDetail jobDetail = createJobDetail(jobName, JOB_GROUP_NAME, clazz);
            CronTrigger trigger = createCronTrigger(jobName, TRIGGER_GROUP_NAME, cron);
            scheduler.scheduleJob(jobDetail, trigger);
            if (!scheduler.isShutdown()) {
                scheduler.start();
            }
        } catch (SchedulerException | ParseException e) {
            log.error(e.getMessage(), e);
        }
    }

    /**
     * 添加一个定时任务
     *
     * @param jobName           job name
     * @param jobGroupName      job组名
     * @param triggerName       触发器名
     * @param triggerGroupName  触发器组名
     * @param clazz             job clazz
     * @param cron              cron表达式
     */
    public static void addJob(String jobName, String jobGroupName,
                              String triggerName, String triggerGroupName, Class<? extends Job> clazz,
                              String cron) {
        try {
            Scheduler scheduler = NeutronHolder.getScheduler();
            JobDetail jobDetail = createJobDetail(jobName, jobGroupName, clazz);
            CronTrigger trigger = createCronTrigger(jobName, TRIGGER_GROUP_NAME, cron);
            scheduler.scheduleJob(jobDetail, trigger);
        } catch (SchedulerException | ParseException e) {
            log.error(e.getMessage(), e);
        }

    }

    /**
     * 修改一个任务的触发时间(使用默认的任务组名，触发器名，触发器组名)
     *
     * @param jobName   job name
     * @param cron      cron表达式
     */
    public static void modifyJobTime(String jobName, String cron) {
        modifyJobTime(jobName, JOB_GROUP_NAME, jobName, TRIGGER_GROUP_NAME, cron);
    }

    /**
     * 修改一个任务的触发时间
     *
     * @param jobName           job name
     * @param jobGroupName      job组名
     * @param triggerName       触发器名
     * @param triggerGroupName  触发器组名
     * @param cron              cron表达式
     */
    public static void modifyJobTime(String jobName, String jobGroupName,
                                     String triggerName, String triggerGroupName, String cron) {
        try {
            Scheduler scheduler = NeutronHolder.getScheduler();
            CronTrigger trigger = (CronTrigger) scheduler.getTrigger(new TriggerKey(triggerName,
                    triggerGroupName));
            if (trigger == null) {
                return;
            }
            String oldTime = trigger.getCronExpression();
            if (!oldTime.equalsIgnoreCase(cron)) {
                JobDetail jobDetail = scheduler.getJobDetail(new JobKey(jobName,
                        jobGroupName));
                Class<? extends Job> objJobClass = jobDetail.getJobClass();
                removeJob(jobName);
                addJob(jobName, objJobClass, cron);
            }
        } catch (SchedulerException e) {
            log.error(e.getMessage(), e);
        }
    }

    /**
     * 移除一个任务(使用默认的任务组名，触发器名，触发器组名)
     *
     * @param jobName job name
     */
    public static void removeJob(String jobName) {
        removeJob(jobName,JOB_GROUP_NAME, jobName, TRIGGER_GROUP_NAME);
    }

    /**
     * 移除一个任务
     *
     * @param jobName           job name
     * @param jobGroupName      job组名
     * @param triggerName       触发器名
     * @param triggerGroupName  触发器组名
     */
    public static void removeJob(String jobName, String jobGroupName,
                                 String triggerName, String triggerGroupName) {
        try {
            Scheduler scheduler = NeutronHolder.getScheduler();
            TriggerKey triggerKey = new TriggerKey(triggerName, triggerGroupName);
            JobKey jobKey = new JobKey(jobName, jobGroupName);
            scheduler.pauseTrigger(triggerKey);// 停止触发器
            scheduler.unscheduleJob(triggerKey);// 移除触发器
            scheduler.deleteJob(jobKey);// 删除任务
        } catch (SchedulerException e) {
            log.error(e.getMessage(), e);
        }
    }

    /**
     * 立即执行任务(使用默认的任务组名，触发器名，触发器组名)
     *
     * @param jobName job name
     */
    public static void startJobNow(String jobName) {
        try {
            Scheduler scheduler = NeutronHolder.getScheduler();
            scheduler.triggerJob(new JobKey(jobName, JOB_GROUP_NAME));
        } catch (SchedulerException e) {
            log.error(e.getMessage(), e);
        }

    }

    /**
     * 立即执行任务
     *
     * @param jobName       job name
     * @param jobGroupName  job组名
     */
    public static void startJobNow(String jobName, String jobGroupName) {
        try {
            Scheduler scheduler = NeutronHolder.getScheduler();
            scheduler.triggerJob(new JobKey(jobName, jobGroupName));
        } catch (SchedulerException e) {
            log.error(e.getMessage(),e);
        }
    }

    /**
     * 启动所有定时任务
     */
    public static void startJobs() {
        try {
            Scheduler scheduler = NeutronHolder.getScheduler();
            scheduler.start();
        } catch (SchedulerException e) {
            log.error(e.getMessage(),e);
        }
    }

    /**
     * 关闭所有定时任务
     */
    public static void shutdownJobs() {
        try {
            Scheduler scheduler = NeutronHolder.getScheduler();
            if (!scheduler.isShutdown()) {
                scheduler.shutdown();
            }
        } catch (SchedulerException e) {
            log.error(e.getMessage(),e);
        }
    }
}
