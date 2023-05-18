/*
 * Copyright (c) 2021 yizzuide All rights Reserved.
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

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
 * @version 3.14.0
 * <br>
 * Create at 2019/11/16 15:30
 */
@Data
@NoArgsConstructor
public class DelayJob implements Serializable {

    private static final long serialVersionUID = -1408197881231593037L;

    /**
     * 延迟任务的唯一标识（当长度都小于64字节时，可用于Redis ZSet数据结构SkipList -> ZipList的存储优化）
     * 内部存储格式：jodId#retryCount
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
        this.topic = job.getTopic();
        this.retryCount = 0;
        this.delayTime = System.currentTimeMillis() + job.getDelay();
    }

    /**
     * Update delay time from dead queue
     * @since 3.14.0
     */
    public void updateDelayTime() {
        this.delayTime = System.currentTimeMillis() + this.delayTime;
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
        return DelayJob.fromSimple(str, delayTime);
    }

}
