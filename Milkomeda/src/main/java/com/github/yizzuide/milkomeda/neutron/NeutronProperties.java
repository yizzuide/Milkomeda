package com.github.yizzuide.milkomeda.neutron;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * NeutronProperties
 *
 * @author yizzuide
 * @version 1.18.0
 * Create at 2019/12/09 22:54
 */
@Data
@ConfigurationProperties("milkomeda.neutron")
public class NeutronProperties {
    /** 数据库持久化 */
    private boolean serializable = true;

    /** 调度器配置项 */
    private Scheduler scheduler = new Scheduler();

    /** 线程池配置项 */
    private ThreadPool threadPool = new ThreadPool();

    /** 持久化配置项 */
    private JobStore jobStore = new JobStore();

    @Data
    public static class Scheduler {
        /** 调度器的实例名 */
        private String instanceName = "neutron-scheduler";

        /** 调度器的实例ID */
        private String instanceId = "AUTO";

        /** 调度线程是否是后台线程（以Daemon模式运行） */
        private boolean makeSchedulerThreadDaemon = true;
    }

    @Data
    public static class ThreadPool {

        /** 使用的线程池实现 */
        private String threadPoolClass = "org.quartz.simpl.SimpleThreadPool";

        /** 线程池里面创建的线程是否是守护线程 */
        private boolean makeThreadsDaemons = true;

        /** 线程数量 */
        private int threadCount = 20;

        /** 线程优先级 */
        private int threadPriority = 5;

        /** 自创建父线程 */
        private boolean threadsInheritContextClassLoaderOfInitializingThread = true;
    }

    @Data
    public static class JobStore {
        /** 持久化实现方式（JobStoreTX为单机环境，本身有事务处理，集群使用JobStoreCMT，依赖分布式事务） */
        private String jobStoreClass = "org.quartz.impl.jdbcjobstore.JobStoreTX";

        /** 数据库驱动 */
        private String driverDelegateClass = "org.quartz.impl.jdbcjobstore.StdJDBCDelegate";
        /** 持久化表前辍 */
        private String tablePrefix = "QRTZ_";

        /** 是否集群 */
        private boolean clustered = false;

        /** 数据库别名 */
        private String dataSource;

        /** 最大作业延长时间 */
        private long misfireThreshold = 60000;

        /** 调度实例失效的检查时间间隔 */
        private long clusterCheckinInterval = 20000;
    }
}
