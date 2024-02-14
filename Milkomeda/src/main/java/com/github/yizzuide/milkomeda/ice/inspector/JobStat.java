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
import com.github.yizzuide.milkomeda.universe.lang.SetString;
import com.github.yizzuide.milkomeda.util.DateExtensionsKt;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.BoundValueOperations;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import static com.github.yizzuide.milkomeda.util.DataTypeConvertUtil.toLong;

/**
 * Job stat record on inspector is enabled.
 *
 * @since 3.14.0
 * @author yizzuide
 * <br>
 * Create at 2022/10/26 20:00
 */
public final class JobStat {
    public static final String JOB_STAT_COUNT_TOTAL_KEY = "total";
    public static final String JOB_STAT_COUNT_FINISH_TOTAL_KEY = "finish_total";
    public static final String JOB_STAT_COUNT_TOPICS_KEY = "topics";
    public static final String JOB_STAT_COUNT_TODAY_KEY_PREFIX = ":count_today";
    public static final String JOB_STAT_COUNT_FINISH_TODAY_KEY_PREFIX = ":count_finish_today";
    public static final String JOB_STAT_COUNT_TOPIC_KEY_PREFIX = "topic:";
    public static final String JOB_STAT_COUNT_SUCCESS_DAY_KEY_PREFIX = ":success_day_";
    public static final String JOB_STAT_COUNT_FAIL_DAY_KEY_PREFIX = ":fail_day_";

    public JobStatInfo getData() {
        BoundHashOperations<String, Object, Object> hOps = getCountHOps();
        JobStatInfo data = new JobStatInfo();
        Object total = hOps.get(JOB_STAT_COUNT_TOTAL_KEY);
        data.setTotal(toLong(total));

        Object finishTotal = hOps.get(JOB_STAT_COUNT_FINISH_TOTAL_KEY);
        data.setFinishTotal(toLong(finishTotal));

        Object topics = hOps.get(JOB_STAT_COUNT_TOPICS_KEY);
        Map<String, Long> topicsMap = new HashMap<>();
        if (topics != null) {
            Set<String> topicSet = new SetString(topics.toString()).toSet();
            topicSet.forEach(topic -> topicsMap.put(topic, toLong(hOps.get(JOB_STAT_COUNT_TOPIC_KEY_PREFIX + topic))));
        }
        data.setTopics(topicsMap);

        Object todayCount = getCountValOps(JOB_STAT_COUNT_TODAY_KEY_PREFIX).get();
        data.setTodayCount(toLong(todayCount));

        Object finishTodayCount = getCountValOps(JOB_STAT_COUNT_FINISH_TODAY_KEY_PREFIX).get();
        data.setFinishTodayCount(toLong(finishTodayCount));

        // count five days period
        Map<String, Long> successDaysCount = new LinkedHashMap<>();
        Map<String, Long> failDaysCount = new LinkedHashMap<>();
        genFiveDaysCount(JOB_STAT_COUNT_SUCCESS_DAY_KEY_PREFIX, successDaysCount);
        genFiveDaysCount(JOB_STAT_COUNT_FAIL_DAY_KEY_PREFIX, failDaysCount);
        data.setSuccessDaysCount(successDaysCount);
        data.setFailDaysCount(failDaysCount);
        return data;
    }

    public void addJob(JobWrapper jobWrapper) {
        // count total
        totalCount(JOB_STAT_COUNT_TOTAL_KEY, JOB_STAT_COUNT_TODAY_KEY_PREFIX);

        // count topic
        BoundHashOperations<String, Object, Object> hOps = getCountHOps();
        hOps.increment(JOB_STAT_COUNT_TOPIC_KEY_PREFIX + jobWrapper.getTopic(), 1);

        // count topics
        Object topics = hOps.get(JOB_STAT_COUNT_TOPICS_KEY);
        if (topics == null) {
            hOps.put(JOB_STAT_COUNT_TOPICS_KEY, jobWrapper.getTopic());
        } else {
            SetString setString = new SetString(topics.toString());
            if (setString.add(jobWrapper.getTopic())) {
                hOps.put(JOB_STAT_COUNT_TOPICS_KEY, setString.toString());
            }
        }
    }

    public void update(JobWrapper jobWrapper) {
        if (jobWrapper.getQueueType() == JobQueueType.JobPool ||
                jobWrapper.getQueueType() == JobQueueType.DeadQueue) {
            // count fail today
            dayCount(buildToDayKey(JOB_STAT_COUNT_FAIL_DAY_KEY_PREFIX), 5L);
        }
    }

    public void finishJob(JobWrapper jobWrapper) {
        // TTR finish!
        if (jobWrapper == null) {
            return;
        }
        // count total
        totalCount(JOB_STAT_COUNT_FINISH_TOTAL_KEY, JOB_STAT_COUNT_FINISH_TODAY_KEY_PREFIX);

        // count topic
        BoundHashOperations<String, Object, Object> hOps = getCountHOps();
        Object topicVal = hOps.get(JOB_STAT_COUNT_TOPIC_KEY_PREFIX + jobWrapper.getTopic());
        if (topicVal != null) {
            hOps.put(JOB_STAT_COUNT_TOPIC_KEY_PREFIX + jobWrapper.getTopic(), String.valueOf(Long.parseLong(topicVal.toString()) - 1));
        }

        // count success today
        dayCount(buildToDayKey(JOB_STAT_COUNT_SUCCESS_DAY_KEY_PREFIX), 5L);
    }

    private void genFiveDaysCount(String key, Map<String, Long> container) {
        for (int i = 4; i >= 0; i--) {
            String day = buildDay(-i);
            String dayKey = key + day;
            String count = getCountValOps(dayKey).get();
            container.put(day, toLong(count));
        }
    }

    private String buildDay(long daysToAdd) {
        LocalDate localDate;
        if (daysToAdd == 0) {
            localDate = LocalDate.now();
        } else {
            localDate = LocalDate.now().plusDays(daysToAdd);
        }
        return localDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
    }

    private String buildToDayKey(String keyPrefix) {
        return keyPrefix + buildDay(0);
    }

    private void totalCount(String totalKey, String todayKey) {
        BoundHashOperations<String, Object, Object> hOps = getCountHOps();
        // count jobs
        Boolean success = hOps.putIfAbsent(totalKey, "1");
        if (!Boolean.TRUE.equals(success)) {
            hOps.increment(totalKey, 1);
        }
        // count jobs today
        dayCount(todayKey, 1L);
    }

    private void dayCount(String key, Long daysOfExpire) {
        BoundValueOperations<String, String> valueOps = getCountValOps(key);
        Boolean success = valueOps.setIfAbsent("1", DateExtensionsKt.timestampOfDays(daysOfExpire), TimeUnit.MILLISECONDS);
        if (!Boolean.TRUE.equals(success)) {
            valueOps.increment();
        }
    }

    private BoundValueOperations<String, String> getCountValOps(String key) {
        return IceHolder.getRedisTemplate().boundValueOps(IceKeys.JOB_STAT_KEY_PREFIX + key);
    }

    private BoundHashOperations<String, Object, Object> getCountHOps() {
        return IceHolder.getRedisTemplate().boundHashOps(IceKeys.JOB_STAT_KEY_PREFIX + ":count");
    }
}
