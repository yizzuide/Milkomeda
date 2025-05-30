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

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.convert.DurationUnit;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.List;

/**
 * AtomProperties
 *
 * @author yizzuide
 * @since 3.3.0
 * @version 3.21.0
 * <br>
 * Create at 2020/04/30 15:26
 */
@Data
@ConfigurationProperties(AtomProperties.PREFIX)
public class AtomProperties {

   public static final String PREFIX = "milkomeda.atom";

    /**
     * 策略方式
     */
    private AtomStrategyType strategy = AtomStrategyType.REDIS;

    /**
     * Zk配置
     */
    private Zk zk = new Zk();

    /**
     * Etcd config.
     * @since 3.15.0
     */
    private Etcd etcd = new Etcd();

    @Data
    static class Etcd {
        /**
         * The URL is used to Connect from ETCD server.
         */
        private String endpointUrl;

        /**
         * The URL is used to Connect from ETCD servers.
         */
        private List<String> endpointUrls;

        /**
         * config etcd auth user.
         */
        private String user;

        /**
         * Etcd auth password.
         */
        private String password;

        /**
         * Sets the authority used to authenticate connections to servers.
         */
        private String authority;

        /**
         * Etcd root lock key.
         */
        private String rootLockNode;

        /**
         * Etcd connect timeout.
         */
        @DurationUnit(ChronoUnit.MILLIS)
        private Duration connectTimeout = Duration.ofMillis(30000);

        /**
         * Set the interval for gRPC keepalive time.
         */
        @DurationUnit(ChronoUnit.MILLIS)
        private Duration keepaliveTime = Duration.ofMillis(30000);

        /**
         * Set timeout for gRPC keepalive time.
         */
        @DurationUnit(ChronoUnit.MILLIS)
        private Duration keepaliveTimeout = Duration.ofMillis(10000);
    }

    @Data
    static class Zk {
        /**
         * 连接地址
         */
        private String address = "127.0.0.1:2181";

        /**
         * 根节点名称
         */
        private String rootLockNode = "mk_atom_locks";

        /**
         * 重试睡眠时间
         */
        @DurationUnit(ChronoUnit.MILLIS)
        private Duration sleepTime = Duration.ofMillis(1000);

        /**
         * 最大重试次数
         */
        private int maxRetry = 3;
    }
}
