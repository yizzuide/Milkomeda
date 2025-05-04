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

import com.github.yizzuide.milkomeda.universe.config.RedissonConfig;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CachingConfigurer;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.interceptor.CacheResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * This cache configuration implements {@link CachingConfigurer} interface for override and return custom {@link CacheManager}.
 * The {@link CacheManager} is responsible for managing the lifecycle of caches and providing access to the appropriate cache implementation (e.g., local or distributed).
 *
 * @since 4.0.0
 * @author yizzuide
 * Create at 2025/05/01 03:18
 */
@EnableCaching
@EnableConfigurationProperties(EuropaProperties.class)
@Import(RedissonConfig.class)
@ConditionalOnProperty(prefix = EuropaProperties.PREFIX, name = "only-cache-l2", havingValue = "false", matchIfMissing = true)
@Configuration
public class CacheConfig implements CachingConfigurer {

    @Autowired
    private EuropaProperties europaProperties;

    @Autowired
    private RedissonClient redissonClient;

    @Bean
    @Override
    public CacheManager cacheManager() {
        return new CaffeineNamedCacheManager(europaProperties);
    }

    @Bean
    public CacheEntryModifiedListener cacheEntryRemovedListener() {
        return new CacheEntryModifiedListener(cacheManager());
    }

    @Bean
    @Override
    public CacheResolver cacheResolver() {
        return new LocalCacheResolver(
                cacheManager(),
                redissonClient,
                cacheEntryRemovedListener());
    }
}
