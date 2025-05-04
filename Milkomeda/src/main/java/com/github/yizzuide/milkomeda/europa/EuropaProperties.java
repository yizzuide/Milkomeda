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

package com.github.yizzuide.milkomeda.europa;

import com.github.yizzuide.milkomeda.universe.context.ApplicationContextHolder;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.convert.DurationUnit;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Cache config properties.
 *
 * @since 4.0.0
 * @author yizzuide
 * Create at 2025/05/01 18:01
 */
@Data
@ConfigurationProperties(prefix = EuropaProperties.PREFIX)
public class EuropaProperties {

    public static final String PREFIX = "milkomeda.europa";

    /**
     * Only enabled L2 cache.
     */
    private boolean onlyCacheL2 = false;

    /**
     * Cache instance config.
     */
    private Map<String, CacheProps> instances = new HashMap<>();

    @Data
    static class CacheProps {

        /**
         * Expiration time for L1 cache.
         */
        private Duration l1Ttl = Duration.of(30, ChronoUnit.MINUTES);

        /**
         * L1 cache max size limited.
         */
        private Integer l1MaxSize = 200;

        /**
         * Expiration time for Redis cache.
         */
        @DurationUnit(ChronoUnit.SECONDS)
        private Duration l2Ttl = Duration.ofHours(2);

        /**
         * Redis cache max idle time.
         */
        @DurationUnit(ChronoUnit.SECONDS)
        private Duration l2MaxIdleTime = Duration.ofMinutes(15);

        /**
         * Redis cache max size of map.
         */
        private Integer l2MaxSize = 0;
    }

    /**
     * 根据缓存名获取TTL时间
     * @param cacheName 缓存名
     * @return 缓存TTL时间
     */
    public static Duration getL2ExpirationTime(String cacheName) {
        EuropaProperties europaProperties = ApplicationContextHolder.get().getBean(EuropaProperties.class);
        Optional<CacheProps> cacheProps = europaProperties.getInstances()
                .keySet()
                .stream()
                .filter(key -> key.equals(cacheName))
                .map(key -> europaProperties.getInstances().get(key))
                .findFirst();
        if (cacheProps.isPresent()) {
            return cacheProps.get().getL2Ttl();
        }
        return Duration.ofHours(2);
    }
}
