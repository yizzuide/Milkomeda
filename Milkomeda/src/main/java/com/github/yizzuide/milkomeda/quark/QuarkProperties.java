/*
 * Copyright (c) 2023 yizzuide All rights Reserved.
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

package com.github.yizzuide.milkomeda.quark;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.convert.DurationUnit;

import java.time.Duration;
import java.time.temporal.ChronoUnit;

/**
 * Quark config properties.
 *
 * @since 3.15.0
 * @version 3.20.0
 * @author yizzuide
 * Create at 2023/08/19 10:04
 */
@Data
@ConfigurationProperties(prefix = "milkomeda.quark")
public class QuarkProperties {

    /**
     * Cache buffer size.
     */
    private Integer bufferSize = 1 << 13;

    /**
     * Buffer size warning percent for expansion.
     * @since 3.20.0
     */
    private Float warningPercent = .05f;

    /**
     * Thread pool.
     */
    private Pool pool = new Pool();


    @Data
    static class Pool {

        private Integer core = 4;

        private Integer maximum = 8;

        @DurationUnit(ChronoUnit.MILLIS)
        private Duration keepAliveTime = Duration.ofSeconds(20);

        private Integer queueSize = 1 << 15;
    }
}
