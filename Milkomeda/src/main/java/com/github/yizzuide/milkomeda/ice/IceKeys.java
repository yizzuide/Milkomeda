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

package com.github.yizzuide.milkomeda.ice;

import java.util.HashMap;
import java.util.Map;

/**
 * IceKeys
 *
 * @author yizzuide
 * @since 3.14.0
 * Create at 2022/09/14 23:50
 */
public class IceKeys {
    public static final String JOB_POOL_KEY_PREFIX = "ice:job_pool";
    public static final String DELAY_BUCKET_KEY_PREFIX = "ice:bucket";
    public static final String READY_QUEUE_KEY_PREFIX = "ice:ready_queue";
    public static final String DEAD_QUEUE_KEY_PREFIX = "ice:dead_queue";
    public static final String JOB_INSPECTOR_CURSOR_KEY_PREFIX = "ice:job_inspect:cursor";
    public static final String JOB_INSPECTOR_Data_KEY_PREFIX = "ice:job_inspect:data";

    /**
     * Resolve the key according to the JobWrapper object.
     * @param jobWrapper  JobWrapper
     * @return  key list
     */
    public static Map<String, String> resolve(JobWrapper jobWrapper) {
        String applicationName = IceHolder.getApplicationName();
        Map<String, String> keyMap = new HashMap<>(3);
        keyMap.put("jobPool", JOB_POOL_KEY_PREFIX.concat(":" + applicationName));
        switch (jobWrapper.getQueueType()) {
            case DelayQueue:
                keyMap.put("delayBucket", DELAY_BUCKET_KEY_PREFIX.concat(jobWrapper.getBucketIndex() + ":" + applicationName));
                break;
            case ReadyQueue:
                keyMap.put("readyQueue", READY_QUEUE_KEY_PREFIX.concat(":" + applicationName + ":" + jobWrapper.getTopic()));
                break;
            case NoneQueue:
                break;
            case DeadQueue:
                keyMap.put("deadQueue", IceKeys.DEAD_QUEUE_KEY_PREFIX.concat(":" + applicationName));
                break;
        }
        return keyMap;
    }
}
