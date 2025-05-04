/*
 * Copyright (c) 2025 yizzuide All rights Reserved.
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

package com.github.yizzuide.milkomeda.universe.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.convert.DurationUnit;

import java.time.Duration;
import java.time.temporal.ChronoUnit;

/**
 * MilkomedaDataProperties
 *
 * @since 4.0.0
 * @author yizzuide
 * Create at 2025/05/01 01:44
 */
@Data
@ConfigurationProperties(MilkomedaDataProperties.PREFIX)
public class MilkomedaDataProperties {

    public static final String PREFIX = "milkomeda.data";

    private Redisson redisson = new Redisson();

    @Data
    static class Redisson {
        /**
         * 是否开启
         */
        private boolean enable = false;

        /**
         * 是否为集群方式（默认为单机模式）
         */
        private boolean useCluster = false;

        /**
         * 集群状态扫描间隔（默认为2s）
         * @since 4.0.0
         */
        @DurationUnit(ChronoUnit.MILLIS)
        private Duration scanInterval = Duration.ofMillis(2000);

        /**
         * 空闲连接超时时间（默认为10s）
         * @since 4.0.0
         */
        @DurationUnit(ChronoUnit.MILLIS)
        private Duration idleConnectionTimeout = Duration.ofMillis(10000);
    }
}
