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
import com.github.yizzuide.milkomeda.util.JSONUtil;
import org.redisson.api.RedissonClient;
import org.redisson.codec.JsonJacksonCodec;
import org.redisson.spring.cache.CacheConfig;
import org.redisson.spring.cache.RedissonSpringCacheManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CachingConfigurer;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import java.util.HashMap;
import java.util.Map;

/**
 * This cache configuration implement {@link CachingConfigurer} interface for override and return {@link RedissonSpringCacheManager}.
 *
 * @since 4.0.0
 * @author yizzuide
 * Create at 2025/05/02 19:40
 */
@EnableCaching
@EnableConfigurationProperties(EuropaProperties.class)
@Import(RedissonConfig.class)
@ConditionalOnProperty(prefix = EuropaProperties.PREFIX, name = "only-cache-l2", havingValue = "true")
@Configuration
public class L2CacheConfig implements CachingConfigurer {

    @Autowired
    private EuropaProperties europaProperties;

    @Bean
    public CacheManager cacheManager(RedissonClient redissonClient) {
        Map<String, CacheConfig> config = new HashMap<>();
        europaProperties.getInstances().keySet().forEach(cacheName -> {
            EuropaProperties.CacheProps cacheProps = europaProperties.getInstances().get(cacheName);
            CacheConfig cacheConfig = new CacheConfig(cacheProps.getL2Ttl().toMillis(), cacheProps.getL2MaxIdleTime().toMillis());
            cacheConfig.setMaxSize(cacheProps.getL2MaxSize());
            config.put(cacheName, cacheConfig);
        });
        // RedissonSpringCacheManager不会直接在Redis键上设置TTL，Redis中的键TTL会被设置为-1（永不过期）。
        // Redisson内部使用自己的数据结构（redisson__timeout__set和redisson__idle__set）来管理过期。
        // 过期清理是通过Redisson的内部调度任务完成的，而不是依赖Redis的过期机制（因为这样可以实现每个Hash子项都有自己的过期时间）。
        return new RedissonSpringCacheManager(redissonClient, config, new JsonJacksonCodec(JSONUtil.mapper));
    }
}
