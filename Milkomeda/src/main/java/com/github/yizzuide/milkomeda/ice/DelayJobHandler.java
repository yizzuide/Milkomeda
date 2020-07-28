package com.github.yizzuide.milkomeda.ice;

import com.github.yizzuide.milkomeda.universe.metadata.HandlerMetaData;
import com.github.yizzuide.milkomeda.universe.polyfill.RedisPolyfill;
import com.github.yizzuide.milkomeda.util.RedisUtil;
import com.github.yizzuide.milkomeda.util.ReflectUtil;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationListener;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.util.CollectionUtils;

import java.util.Collections;
import java.util.List;

/**
 * DelayJobHandler
 *
 * @author yizzuide
 * @since 1.15.0
 * @version 3.12.0
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
    @SuppressWarnings({"unchecked", "rawtypes"})
    private void processTtrJob(DelayJob delayJob, Job<?> job) {
        if (delayJob.getRetryCount() > Integer.MAX_VALUE - 1) {
            log.error("Ice处理TTR的Job {}, 重试次数超过Integer.MAX_VALUE，放弃重试", delayJob.getJodId());
            return;
        }
        int currentRetryCount = delayJob.getRetryCount() + 1;
        log.warn("Ice处理TTR的Job {}，当前重试次数为{}", delayJob.getJodId(), currentRetryCount);
        // 检测重试次数过载
        boolean overload = delayJob.getRetryCount() >= job.getRetryCount();
        // 过载处理标识
        boolean handleFlag = false;
        // 调用TTR监听器
        List<HandlerMetaData> ttrMetaDataList = IceContext.getTopicTtrMap().get(job.getTopic());
        if (!CollectionUtils.isEmpty(ttrMetaDataList)) {
            for (HandlerMetaData handlerMetaData : ttrMetaDataList) {
                try {
                    TtrJob<?> ttrJob = new TtrJob<>();
                    ttrJob.setOverload(overload);
                    ttrJob.setRetryCount(currentRetryCount);
                    ttrJob.setJob((Job) job);
                    ReflectUtil.invokeWithWrapperInject(handlerMetaData.getTarget(), handlerMetaData.getMethod(), Collections.singletonList(ttrJob), TtrJob.class, tj -> tj.getJob().getBody(), (tj, body) -> tj.getJob().setBody(body));
                    handleFlag = true;
                } catch (Exception e) {
                    log.error("Ice invoke TTR listener error: {}", e.getMessage(), e);
                }
            }
        }

        if (overload) {
            log.error("Ice检测到 Job {} 的TTR超过预设的{}次!", job.getId(), job.getRetryCount());
            // 调用TTR Overload监听器
            List<HandlerMetaData> ttrOverloadMetaDataList = IceContext.getTopicTtrOverloadMap().get(job.getTopic());
            if (!CollectionUtils.isEmpty(ttrOverloadMetaDataList)) {
                for (HandlerMetaData handlerMetaData : ttrOverloadMetaDataList) {
                    try {
                        ReflectUtil.invokeWithWrapperInject(handlerMetaData.getTarget(), handlerMetaData.getMethod(), Collections.singletonList(job), Job.class, Job::getBody, Job::setBody);
                        handleFlag = true;
                    } catch (Exception e) {
                        log.error("Ice invoke TTR overload listener error: {}", e.getMessage(), e);
                    }
                }
            }
            boolean finalHandleFlag = handleFlag;
            RedisUtil.batchOps((operations) -> {
                // 如果调用TTR Overload监听成功
                if (finalHandleFlag) {
                    // 移除delayBucket中的任务
                    delayBucket.remove(operations, index, delayJob);
                    // 没有开启Dead queue就移除原数据
                    if (!props.isEnableRetainToDeadQueueWhenTtrOverload()) {
                        IceHolder.getIce().finish(operations, job.getId());
                    }
                }
                if (props.isEnableRetainToDeadQueueWhenTtrOverload()) {
                    // 没有TTR Overload处理器时，从延迟队列移除
                    if (!finalHandleFlag) {
                        delayBucket.remove(operations, index, delayJob);
                    }
                    // 修改池中状态
                    job.setStatus(JobStatus.DELAY);
                    jobPool.push(operations, job);
                    // 重置延迟作业状态
                    delayJob.setDelayTime(job.getDelay());
                    delayJob.setRetryCount(0);
                    // 添加dead queue
                    deadQueue.add(operations, delayJob);
                    log.warn("Ice处理TTR Overload的 Job {} 进入Dead queue", job.getId());
                }
            }, redisTemplate);

            // 如果有处理方案，不再重试
            if (handleFlag || props.isEnableRetainToDeadQueueWhenTtrOverload()) {
                return;
            }
        }

        // 重置到当前延迟
        long delayDate = System.currentTimeMillis() +
                (props.isEnableDelayMultiRetryCount() ?
                        job.getDelay() * (currentRetryCount) * props.getRetryDelayMultiFactor() :
                        job.getDelay());
        RedisUtil.batchOps((operations) -> {
            // 还原到延迟状态
            job.setStatus(JobStatus.DELAY);
            jobPool.push(operations, job);
            // 移除delayBucket中的任务
            delayBucket.remove(operations, index, delayJob);
            // 设置当前重试次数
            delayJob.setRetryCount(currentRetryCount);
            delayJob.setDelayTime(delayDate);
            // 再次添加到任务中
            delayBucket.add(operations, delayJob);
        }, redisTemplate);
    }

    /**
     * 处理延时任务
     */
    private void processDelayJob(DelayJob delayJob, Job<?> job) {
        log.info("Ice正在处理延迟的Job {}，当前状态为：{}", delayJob.getJodId(), job.getStatus());
        RedisUtil.batchOps((operations) -> {
            // 修改任务池状态
            job.setStatus(JobStatus.READY);
            jobPool.push(operations, job);
            // 设置到待处理任务
            readyQueue.push(operations, delayJob);
            // 移除delayBucket中的任务
            delayBucket.remove(operations, index, delayJob);
        }, redisTemplate);
    }

    @Override
    public void onApplicationEvent(IceInstanceChangeEvent event) {
        String instanceName = event.getSource().toString();
        this.lockKey = "ice:execute_delay_bucket_lock_" + this.index + ":" + instanceName;
    }
}
