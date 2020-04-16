package com.github.yizzuide.milkomeda.ice;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.convert.DurationUnit;

import java.time.Duration;
import java.time.temporal.ChronoUnit;

/**
 * IceProperties
 *
 * @author yizzuide
 * @since 1.15.0
 * @version 3.0.7
 * Create at 2019/11/16 19:02
 */
@Data
@ConfigurationProperties("milkomeda.ice")
class IceProperties {
    /**
     * 开启作业Timer（仅作为消费端使用时需要设置为false）<br>
     * 注意：使用 {@link EnableIceServer} 时，设置为false无效
     */
    private boolean enableJobTimer = true;
    /**
     * 开启JobTimer分布式并发功能（分布式应该设置为true）
     */
    private boolean enableJobTimerDistributed = false;
    /**
     * 多个JobTimer的并发锁超时（单位：s）
     */
    @DurationUnit(ChronoUnit.SECONDS)
    private Duration jobTimerLockTimeoutSeconds = Duration.ofSeconds(60);
    /**
     * 延迟分桶数量
     */
    private int delayBucketCount = 3;
    /**
     * 延迟分桶任务轮询间隔（单位：ms）
     */
    @DurationUnit(ChronoUnit.MILLIS)
    private Duration delayBucketPollRate = Duration.ofMillis(5000);
    /**
     * 任务执行超时时间（单位：ms）
     */
    @DurationUnit(ChronoUnit.MILLIS)
    private Duration ttr = Duration.ofMillis(30000);
    /**
     * 任务执行超时重试次数
     */
    private int retryCount = 3;
    /**
     * 开启任务执行超时重试时，延迟时间根据重试次数递增
     */
    private boolean enableDelayMultiRetryCount = true;
    /**
     * 任务执行超时重试的延迟时间增长因子（设置为1则使用原有延迟时间）
     */
    private int retryDelayMultiFactor = 1;

    /**
     * 使用Task处理方式接收Topic方式<br>
     * 启用后，才可以通过 {@link IceHandler} 和 {@link IceListener} 接收Topic<br>
     * 注意：使用 {@link EnableIceClient} 时，设置为false无效
     */
    private boolean enableTask = false;

    /**
     * 任务池大小
     * @deprecated since 1.16.0, instead of spring.scheduling.pool.size
     */
    private int taskPoolSize = 20;

    /**
     * 任务被停止时的有效执行时间（单位：s）
     * @deprecated since 1.16.0, instead of spring.scheduling.shutdown.await-termination-period
     */
    @DurationUnit(ChronoUnit.SECONDS)
    private Duration taskTerminationAwareSeconds = Duration.ofSeconds(60);

    /**
     * Topic任务消费的最大个数
     */
    private int taskTopicPopMaxSize = 10;

    /**
     * 任务执行间隔（单位：ms）
     */
    @DurationUnit(ChronoUnit.MILLIS)
    private Duration taskExecuteRate = Duration.ofMillis(5000);

    /**
     * 多个消费的并发锁超时（单位：s）
     */
    @DurationUnit(ChronoUnit.SECONDS)
    private Duration taskPopCountLockTimeoutSeconds = Duration.ofSeconds(60);

    /**
     * 开启消费处理器扫描多个Topic监听器
     */
    private boolean multiTopicListenerPerHandler = false;
}
