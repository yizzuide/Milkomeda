package com.github.yizzuide.milkomeda.atom;

import lombok.Builder;
import lombok.Data;

/**
 * AtomLockInfo
 *
 * @author yizzuide
 * @since 3.3.0
 * Create at 2020/05/04 00:55
 */
@Data
@Builder
public class AtomLockInfo {
    /**
     * 是否加锁
     */
    private boolean isLocked;
    /**
     * 锁对象
     */
    private Object lock;
}
