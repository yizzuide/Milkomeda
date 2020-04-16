package com.github.yizzuide.milkomeda.ice;

import com.github.yizzuide.milkomeda.universe.metadata.HandlerMetaData;
import com.github.yizzuide.milkomeda.universe.polyfill.RedisPolyfill;
import com.github.yizzuide.milkomeda.util.RedisUtil;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationListener;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.util.CollectionUtils;

import java.lang.reflect.Method;
import java.util.List;

/**
 * DelayJobHandler
 *
 * @author yizzuide
 * @since 1.15.0
 * @version 3.0.8
 * Create at 2019/11/16 17:30
 */
@Slf4j
@Data
public class DelayJobHandler implements Runnable, ApplicationListener<IceInstanceChangeEvent> {

    private IceProperties props;

    private StringRedisTemplate redisTemplate;

    /**
     * 任务池
     */
    private JobPool jobPool;

    /**
     * 延迟队列
     */
    private DelayBucket delayBucket;

    /**
     * 待处理队列
     */
    private ReadyQueue readyQueue;

    /**
     * TTR Overload队列
     */
    private DeadQueue deadQueue;

    /**
     * 索引
     */
    private int index;

    // 延迟桶分布式锁Key
    private String lockKey;

    public void fill(StringRedisTemplate redisTemplate, JobPool jobPool, DelayBucket delayBucket, ReadyQueue readyQueue, DeadQueue deadQueue, int i, IceProperties props) {
        this.redisTemplate = redisTemplate;
        this.jobPool = jobPool;
        this.delayBucket = delayBucket;
        this.readyQueue = readyQueue;
        this.deadQueue = deadQueue;
        this.index = i;
        this.props = props;
        if (IceProperties.DEFAULT_INSTANCE_NAME.equals(props.getInstanceName())) {
            this.lockKey = "ice:execute_delay_bucket_lock_" + i;
        } else {
            this.lockKey = "ice:execute_delay_bucket_lock_" + i + ":" + props.getInstanceName();
        }
    }

    @Override
    public void run() {
        // 延迟桶处理锁住资源，防止同一桶索引分布式并发执行时出现相同记录问题
        if (props.isEnableJobTimerDistributed()) {
            boolean hasObtainLock = RedisUtil.setIfAbsent(this.lockKey, props.getJobTimerLockTimeoutSeconds().getSeconds(), redisTemplate);
            if (!hasObtainLock) return;
        }

        DelayJob delayJob = null;
        try {
            delayJob = delayBucket.poll(index);
            // 没有任务
            if (delayJob == null) {
                return;
            }

            // 延时任务的延迟时间还没到
            long currentTime = System.currentTimeMillis();
            if (delayJob.getDelayTime() > currentTime) {
                return;
            }

            // 获取超时任务（包括延迟超时和TTR超时）
            Job<?> job = jobPool.get(delayJob.getJodId());
            // 任务元数据不存在（说明任务已被消费）
            if (job == null) {
                // 移除TTR超时检测任务
                delayBucket.remove(index, delayJob);
                return;
            }
            JobStatus status = job.getStatus();
            if (JobStatus.RESERVED.equals(status)) {
                // 处理超时任务
                processTtrJob(delayJob, job);
            } else {
                // 延时任务
                processDelayJob(delayJob, job);
            }
        } catch (Exception e) {
            log.error("Ice Timer处理延迟Job {} 异常：{}", delayJob != null ?
                    delayJob.getJodId()  : "[任务数据获取失败]", e.getMessage(), e);
        } finally {
            if (props.isEnableJobTimerDistributed()) {
                // 删除Lock
                RedisPolyfill.redisDelete(redisTemplate, this.lockKey);
            }
        }
    }

    /**
     * 处理ttr的任务
     */
    private void processTtrJob(DelayJob delayJob, Job<?> job) {
        log.info("Ice处理TTR的Job {}，当前重试次数为{}", delayJob.getJodId(), delayJob.getRetryCount() + 1);
        // 检测重试次数过载
        boolean overload = delayJob.getRetryCount() >= job.getRetryCount();
        if (overload) {
            log.error("Ice检测到 Job {} 的TTR超过预设的{}次，将进入dead queue!", job.getId(), job.getRetryCount());
            // 调用Overload监听器
            List<HandlerMetaData> handlerMetaDataList = IceContext.getTopicTtrOverloadMap().get(job.getTopic());
            if (CollectionUtils.isEmpty(handlerMetaDataList)) {
                return;
            }
            handlerMetaDataList.forEach(handlerMetaData -> {
                Method method = handlerMetaData.getMethod();
                try {
                    method.invoke(handlerMetaData.getTarget(), job);
                } catch (Exception e) {
                    log.error("Ice invoke TTR overload listener error: {}", e.getMessage(), e);
                }
            });
            // 移除delayBucket中的任务
            delayBucket.remove(index, delayJob);
            // 修改池中状态
            job.setStatus(JobStatus.DELAY);
            jobPool.push(job);
            // 重置延迟作业状态
            delayJob.setDelayTime(job.getDelay());
            delayJob.setRetryCount(0);
            // 添加dead queue
            deadQueue.add(delayJob);
            return;
        }

        RedisUtil.batchOps(() -> {
            // 还原到延迟状态
            job.setStatus(JobStatus.DELAY);
            jobPool.push(job);
            // 移除delayBucket中的任务
            delayBucket.remove(index, delayJob);
            // 设置当前重试次数
            if (delayJob.getRetryCount() < Integer.MAX_VALUE - 1) {
                delayJob.setRetryCount(delayJob.getRetryCount() + 1);
            }
            // 重置到当前延迟
            long delayDate = System.currentTimeMillis() +
                    (props.isEnableDelayMultiRetryCount() ?
                            job.getDelay() * (delayJob.getRetryCount() + 1) * props.getRetryDelayMultiFactor() :
                            job.getDelay());
            delayJob.setDelayTime(delayDate);
            // 再次添加到任务中
            delayBucket.add(delayJob);
        }, redisTemplate);
    }

    /**
     * 处理延时任务
     */
    private void processDelayJob(DelayJob delayJob, Job<?> job) {
        log.info("Ice正在处理延迟的Job {}，当前状态为：{}", delayJob.getJodId(), job.getStatus());
        RedisUtil.batchOps(() -> {
            // 修改任务池状态
            job.setStatus(JobStatus.READY);
            jobPool.push(job);
            // 设置到待处理任务
            readyQueue.push(delayJob);
            // 移除delayBucket中的任务
            delayBucket.remove(index, delayJob);
        }, redisTemplate);
    }

    @Override
    public void onApplicationEvent(IceInstanceChangeEvent event) {
        String instanceName = event.getSource().toString();
        this.lockKey = "ice:execute_delay_bucket_lock_" + this.index + ":" + instanceName;
    }
}
