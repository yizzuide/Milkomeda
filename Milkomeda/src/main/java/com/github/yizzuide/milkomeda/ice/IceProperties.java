package com.github.yizzuide.milkomeda.ice;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * IceProperties
 *
 * @author yizzuide
 * @since 1.15.0
 * @version 1.15.2
 * Create at 2019/11/16 19:02
 */
@Data
@ConfigurationProperties("milkomeda.ice")
class IceProperties {
    /**
     * 开启作业Timer（默认为true，仅作为消费端使用时需要设置为false）<br>
     * 注意：使用<code>@EnableIceServer</code>时，设置为false无效
     */
    private boolean enableJobTimer = true;
    /**
     * 延迟分桶数量（默认为3）
     */
    private int delayBucketCount = 3;
    /**
     * 延迟分桶任务轮询间隔（单位：ms，默认为1000）
     */
    private long delayBucketPollRate = 1000;
    /**
     * 任务执行超时时间（单位：ms，默认为5000）
     */
    private long ttr = 5000;
    /**
     * 任务执行超时重试次数（默认为3）
     */
    private int retryCount = 3;
    /**
     * 开启任务执行超时重试时，延迟时间根据重试次数递增（默认为true）
     */
    private boolean enableDelayMultiRetryCount = true;
    /**
     * 任务执行超时重试的延迟时间增长因子（默认为1，使用原有延迟时间）
     */
    private int retryDelayMultiFactor = 1;

    /**
     * 使用Task处理方式接收Topic方式（默认为false)<br>
     * 启用后，才可以通过<code>@IceHandler</code>和<code>@IceListener</code>接收Topic<br>
     * 注意：使用<code>@EnableIceClient</code>时，设置为false无效
     */
    private boolean enableTask = false;

    /**
     * 任务池大小（默认20）
     * @deprecated since 1.16.0, instead of spring.scheduling.pool.size
     */
    private int taskPoolSize = 20;

    /**
     * 任务被停止时的有效执行时间（单位：s，默认60）
     * @deprecated since 1.16.0, instead of spring.scheduling.shutdown.await-termination-period
     */
    private int taskTerminationAwareSeconds = 60;

    /**
     * Topic任务消费的最大个数（默认为10）
     */
    private int taskTopicPopMaxSize = 10;

    /**
     * 任务执行间隔（单位：ms，默认5000）
     */
    private long taskExecuteRate = 5000;

    /**
     * 多个消费的并发锁超时（单位：s，默认60s）
     */
    private long taskPopCountLockTimeoutSeconds = 60L;
}
