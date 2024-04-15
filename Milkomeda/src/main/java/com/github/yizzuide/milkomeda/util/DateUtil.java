/*
 * Copyright (c) 2024 yizzuide All rights Reserved.
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

package com.github.yizzuide.milkomeda.util;

import org.apache.commons.lang3.time.DateUtils;

import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Calendar;
import java.util.Date;
import java.util.stream.Stream;

/**
 * DateUtil
 *
 * @author yizzuide
 * @since 3.20.0
 * Create at 2024/01/13 16:37
 */
public class DateUtil {

    public static final String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";

    public static final String DATE_FORMAT_HHMMSS = "yyyy-MM-dd HH:mm:ss";

    public static int getUnixTime() {
        return getUnixTime(System.currentTimeMillis());
    }

    public static int getUnixTime(long timestamp) {
        return Math.toIntExact(timestamp / 1000);
    }

    public static int getUnixTime(Date date) {
        return getUnixTime(date.getTime());
    }

    public static int getUnixTime(String date) {
        SimpleDateFormat dataFormat = new SimpleDateFormat(DATE_FORMAT);
        try {
            return getUnixTime(dataFormat.parse(date));
        } catch (Exception e) {
            return 0;
        }
    }

    public static int getUnixDay(long unixTime) {
        return getUnixDay(new Date(unixTime * 1000));
    }

    public static int getUnixDay(Date date) {
        return (int) (DateUtils.truncate(date, Calendar.DAY_OF_MONTH).getTime() / 1000);
    }

    public static Instant beginDate(Instant current) {
        return current.truncatedTo(ChronoUnit.DAYS);
    }

    public static long betweenDays(Instant begin, Instant end) {
        return ChronoUnit.DAYS.between(begin, end);
    }

    public static long betweenHours(Instant begin, Instant end) {
        return Duration.between(begin, end).toHours();
    }

    public static long betweenMinutes(Instant begin, Instant end) {
        return ChronoUnit.MINUTES.between(begin, end);
    }

    public static boolean isFuture(Instant current) {
        return current.isAfter(Instant.now());
    }

    public static Integer nextUnixDay(Integer day) {
        return plusUnixDays(day, 1);
    }

    public static Integer plusUnixDays(Integer day, Integer unit) {
        return Math.toIntExact(Instant.ofEpochSecond(day).plus(unit, ChronoUnit.DAYS).getEpochSecond());
    }

    public static Stream<Integer> genUnixDays(Integer beginDay, Integer endDay) {
        return Stream.iterate(beginDay, DateUtil::nextUnixDay)
                .limit(ChronoUnit.DAYS.between(Instant.ofEpochSecond(beginDay), Instant.ofEpochSecond(endDay)));
    }
}
