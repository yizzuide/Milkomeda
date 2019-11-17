package com.github.yizzuide.milkomeda.ice;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * IceProperties
 *
 * @author yizzuide
 * @since 1.15.0
 * Create at 2019/11/16 19:02
 */
@Data
@ConfigurationProperties("milkomeda.ice")
class IceProperties {
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
     * 启用后，才可以通过<code>@IceHandler</code>和<code>@IceListener</code>接收Topic
     */
    private boolean enableTask = false;

    /**
     * 任务池大小（默认20）
     */
    private int taskPoolSize = 20;

    /**
     * 任务被停止时的有效执行时间（单位：s，默认60）
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
}
