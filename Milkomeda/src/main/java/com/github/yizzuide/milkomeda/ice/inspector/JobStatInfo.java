/*
 * Copyright (c) 2022 yizzuide All rights Reserved.
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

package com.github.yizzuide.milkomeda.ice.inspector;

import lombok.Data;

import java.util.Map;

/**
 * Job stat record info.
 *
 * @since 3.14.0
 * @author yizzuide
 * <br>
 * Create at 2022/10/27 18:23
 */
@Data
public class JobStatInfo {
    /**
     * Total of jobs added number.
     */
    private Long total;

    /**
     * Total of has executed completely number.
     */
    private Long finishTotal;

    /**
     * Count of today jobs added number.
     */
    private Long todayCount;

    /**
     * Count of today jobs executed completely number.
     */
    private Long finishTodayCount;

    /**
     * Count of job pool.
     */
    private Long jobPoolCount;

    /**
     * Count of ready queue.
     */
    private Long readyQueueCount;

    /**
     * Count of dead queue.
     */
    private Long deadQueueCount;

    /**
     * Count of delay buckets.
     */
    private Map<String, Long> delayBuckets;

    /**
     * Count of topics added number.
     */
    private Map<String, Long> topics;

    /**
     * Count of successful processing within five days.
     */
    private Map<String, Long> successDaysCount;

    /**
     * Count of failure processing within five days.
     */
    private Map<String, Long> failDaysCount;
}
