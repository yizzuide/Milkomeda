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

package com.github.yizzuide.milkomeda.echo;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.convert.DurationUnit;

import java.time.Duration;
import java.time.temporal.ChronoUnit;

/**
 * EchoProperties
 *
 * @author yizzuide
 * @since 1.13.3
 * @version 4.0.0
 * <br>
 * Create at 2019/10/23 20:53
 */
@Data
@ConfigurationProperties("milkomeda.echo")
public class EchoProperties {
    /**
     * 连接池最大连接数
     */
    private int poolMaxSize = 200;

    /**
     * 每个路由的并发量
     */
    private int defaultMaxPerRoute = 50;

    /**
     * 从池中获取请求连接超时（单位：ms，不宜过长）
     */
    @DurationUnit(ChronoUnit.MILLIS)
    private Duration connectionRequestTimeout = Duration.ofMillis(200);

    /**
     * 连接超时（单位：ms）
     */
    @DurationUnit(ChronoUnit.MILLIS)
    private Duration connectTimeout = Duration.ofMillis(6000);

    /**
     * 响应超时（单位：ms，0为不限制）
     * @since 4.0.0
     */
    @DurationUnit(ChronoUnit.MILLIS)
    private Duration responseTimeout = Duration.ZERO;

    /**
     * 连接保活时长（单位：ms）
     */
    @DurationUnit(ChronoUnit.MILLIS)
    private Duration keepAlive = Duration.ofMillis(5000);

    /**
     * 允许重试
     */
    private boolean enableRequestSentRetry = true;

    /**
     * 重试次数
     */
    private int retryCount = 3;
}
