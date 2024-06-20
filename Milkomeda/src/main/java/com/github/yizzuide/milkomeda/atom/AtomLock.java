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

package com.github.yizzuide.milkomeda.atom;

import java.lang.annotation.*;

/**
 * AtomLock
 *
 * @author yizzuide
 * @since 3.3.0
 * @version 3.3.1
 * <br>
 * Create at 2020/04/30 16:26
 */
@Documented
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface AtomLock {
    /**
     * 分布式key，支持Spring EL
     * @return String
     */
    String key() default "";

    /**
     * 等待获取锁时间ms
     * @return -1等待直到获取锁
     */
    long waitTime() default -1;

    /**
     * 自动释放锁时间ms（Redis设置为-1时开启WatchDog锁续期，ZK不需要这个特性，断开就删除节点）
     * @return -1不自动释放锁
     */
    long leaseTime() default -1;

    /**
     * 加锁类型（ZK仅支持公平锁、读写锁，ETCD不支持设置）
     * @return AtomLockType
     */
    AtomLockType type() default AtomLockType.FAIR;

    /**
     * 操作是否只读（仅支持读写锁类型 {@link AtomLockType#READ_WRITE}）
     * @return true只读
     */
    boolean readOnly() default false;

    /**
     * 锁等待超时处理方案
     * @return AtomLockWaitTimeoutType
     * @since 3.3.1
     */
    AtomLockWaitTimeoutType waitTimeoutType() default AtomLockWaitTimeoutType.THROW_EXCEPTION;

    /**
     * 锁等待超时反馈处理
     * @return Spring EL表达式
     * @since 3.3.1
     */
    String fallback() default "";
}
