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

import com.github.yizzuide.milkomeda.ice.IceHolder;
import com.github.yizzuide.milkomeda.ice.IceKeys;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.BoundValueOperations;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.TimeUnit;

/**
 * Job stat record on inspector is enabled.
 *
 * @since 3.14.0
 * @author yizzuide
 * <br>
 * Create at 2022/10/26 20:00
 */
public class JobStat {

    public void addJob(JobWrapper jobWrapper) {
        totalCount("total", ":count_today");

        // count topic
        BoundHashOperations<String, Object, Object> hOps = getCountHOps();
        hOps.increment("topic:" + jobWrapper.getTopic(), 1);
    }

    public void update(JobWrapper jobWrapper) {
        if (jobWrapper.getQueueType() == JobQueueType.NoneQueue ||
                jobWrapper.getQueueType() == JobQueueType.DeadQueue) {
            // count fail today
            dayCount(false);
        }
    }

    public void finishJob(JobWrapper jobWrapper) {
        // TTR finish!
        if (jobWrapper == null) {
            return;
        }

        totalCount("finish_total", ":count_finish_today");

        // count topic
        BoundHashOperations<String, Object, Object> hOps = getCountHOps();
        Object topicVal = hOps.get("topic:" + jobWrapper.getTopic());
        if (topicVal != null) {
            hOps.put("topic:" + jobWrapper.getTopic(), String.valueOf(Long.parseLong(topicVal.toString()) - 1));
        }

        // count success today
        dayCount(true);
    }

    private void totalCount(String totalKey, String todayKey) {
        BoundHashOperations<String, Object, Object> hOps = getCountHOps();
        // count jobs
        Boolean success = hOps.putIfAbsent(totalKey, "1");
        if (!Boolean.TRUE.equals(success)) {
            hOps.increment(totalKey, 1);
        }

        // count jobs today
        BoundValueOperations<String, String> valueOps = IceHolder.getRedisTemplate().boundValueOps(IceKeys.JOB_STAT_KEY_PREFIX + todayKey);
        long tomorrowMill = LocalDate.now().plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli();
        success = valueOps.setIfAbsent("1", tomorrowMill - System.currentTimeMillis(), TimeUnit.MILLISECONDS);
        if (!Boolean.TRUE.equals(success)) {
            valueOps.increment();
        }
    }

    private void dayCount(boolean isSuccess) {
        LocalDate now = LocalDate.now();
        String day = now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        String key = isSuccess ? ":success_day_" + day : ":fail_day_" + day;
        BoundValueOperations<String, String> valueOps = IceHolder.getRedisTemplate().boundValueOps(IceKeys.JOB_STAT_KEY_PREFIX + key);
        long fiveDayMill = now.plusDays(5).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli();
        Boolean success = valueOps.setIfAbsent("1", fiveDayMill - System.currentTimeMillis(), TimeUnit.MILLISECONDS);
        if (!Boolean.TRUE.equals(success)) {
            valueOps.increment();
        }
    }

    private BoundHashOperations<String, Object, Object> getCountHOps() {
        return IceHolder.getRedisTemplate().boundHashOps(IceKeys.JOB_STAT_KEY_PREFIX + ":count");
    }
}
