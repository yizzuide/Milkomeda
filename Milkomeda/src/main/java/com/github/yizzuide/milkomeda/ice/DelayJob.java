package com.github.yizzuide.milkomeda.ice;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.github.yizzuide.milkomeda.util.JSONUtil;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.util.StringUtils;

import java.io.Serializable;

/**
 * DelayJob
 *
 * @author yizzuide
 * @since 1.15.0
 * @version 3.8.0
 * Create at 2019/11/16 15:30
 */
@Data
@NoArgsConstructor
class DelayJob implements Serializable {
    private static final long serialVersionUID = -1408197881231593037L;

    /**
     * 延迟任务的唯一标识（长度最好小于62位，因为存储重试次数多占了两位，用于Redis的 ziplist 内存存储优化）
     */
    private String jodId;

    /**
     * 任务的执行时间（单位：ms）
     */
    private long delayTime;

    /**
     * 任务分组
     */
    private String topic;

    /**
     * 当前重试次数
     */
    private int retryCount;

    /**
     * 使用高效的字符串行压缩方式（用于兼容旧方式的状态记录）
     */
    @JsonIgnore
    private transient boolean usedSimple = false;

    @SuppressWarnings("rawtypes")
    DelayJob(Job job) {
        this.jodId = job.getId();
        this.delayTime = System.currentTimeMillis() + job.getDelay();
        this.topic = job.getTopic();
    }

    /**
     * 转为字符串行
     * @return  字符串行
     */
    public String toSimple() {
       return this.getJodId() + "#" + this.getRetryCount();
    }

    /**
     * 从字符串行构造
     * @param simple        字符串行
     * @param delayTime     延迟时间
     * @return  DelayJob
     */
    public static DelayJob fromSimple(String simple, Double delayTime) {
        DelayJob delayJob = new DelayJob();
        if (delayTime != null) {
            delayJob.setDelayTime(delayTime.longValue());
        }
        String[] jobIdWithRetryCount = StringUtils.delimitedListToStringArray(simple, "#");
        String jodId = jobIdWithRetryCount[0];
        delayJob.setJodId(jodId);
        delayJob.setRetryCount(Integer.parseInt(jobIdWithRetryCount[1]));
        int index = jodId.lastIndexOf('-');
        String topic = jodId.substring(0, index);
        delayJob.setTopic(topic);
        delayJob.setUsedSimple(true);
        return delayJob;
    }

    /**
     * 兼容新旧方式从字符串构造
     * @param str           对象字符串
     * @param delayTime     延迟时间
     * @return  DelayJob
     */
    public static DelayJob compatibleDecode(String str, Double delayTime) {
        if (str.startsWith("{")) {
            return JSONUtil.parse(str, DelayJob.class);
        }
        return DelayJob.fromSimple(str, null);
    }

}
