package com.github.yizzuide.milkomeda.ice;

import lombok.Data;

/**
 * TtrJob
 * 重试任务信息
 *
 * @author yizzuide
 * @since 3.2.0
 * Create at 2020/04/26 10:52
 */
@Data
public class TtrJob<T> {
    /**
     * 是否重试超载
     */
    private boolean isOverload;

    /**
     * 当前重试次数
     */
    private int retryCount;

    /**
     * 任务
     */
    private Job<T> job;
}
