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

/**
 * JobStatus
 *
 * @author yizzuide
 * @since 1.15.0
 * @version 3.14.0
 * <br />
 * Create at 2019/11/16 12:53
 */
public enum JobStatus {
    /**
     * 延迟中（准备状态）
     */
    DELAY,
    /**
     * 准备消费（可执行状态）
     */
    READY,
    /**
     * 消费中（已被消费者读取）
     */
    RESERVED,
    /**
     * 闲置中（超过重试次数或进入DeadQueue）
     * @since 3.14.0
     */
    IDLE,
    /**
     * 消费完成（由于删除是即时的，所以状态用不上）
     */
    DELETED
}
