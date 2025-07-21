package com.github.yizzuide.milkomeda.demo.molecule.core.uinterface.retry;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;

/**
 * 重推失败异常
 *
 * @author yizzuide
 * Create at 2025/07/11 10:45
 */
@Getter
@AllArgsConstructor
public class RetryFailedException extends RuntimeException {
    @Serial
    private static final long serialVersionUID = 6278971649720320180L;

    /**
     * 失败原因
     */
    private String reason;

    /**
     * 重试次数
     */
    @Setter
    private int retryCount;

    /**
     * 记录数据
     */
    private final Object record;
}