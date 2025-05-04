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

import org.redisson.api.RedissonClient;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.interceptor.CacheOperationInvocationContext;
import org.springframework.cache.interceptor.CacheResolver;
import org.springframework.lang.NonNull;

import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * The {@link CacheResolver} determines which cache(s) to use for a specific operation dynamically.
 * Here, the {@link LocalCacheResolver} bridges the local (Caffeine) and distributed (Redisson) caches,
 * ensuring that the hybrid strategy is applied effectively.
 *
 * @since 4.0.0
 * @author yizzuide
 * Create at 2025/05/01 17:31
 */
public class LocalCacheResolver implements CacheResolver {
    private final CacheManager cacheManager;
    private final RedissonClient redisson;
    private final CacheEntryModifiedListener cacheEntryModifiedListener;
    private final Map<String, LocalCache> cacheMap = new ConcurrentHashMap<>();

    public LocalCacheResolver(
            CacheManager cacheManager,
            RedissonClient redisson,
            CacheEntryModifiedListener cacheEntryModifiedListener) {
        this.cacheManager = cacheManager;
        this.redisson = redisson;
        this.cacheEntryModifiedListener = cacheEntryModifiedListener;
    }

    @Override
    @NonNull
    public Collection<? extends Cache> resolveCaches(
            @NonNull CacheOperationInvocationContext<?> context) {
        Collection<Cache> caches = getCaches(cacheManager, context);
        return caches.stream().map(this::getOrCreateLocalCache).collect(Collectors.toList());
    }

    private Collection<Cache> getCaches(CacheManager cacheManager, CacheOperationInvocationContext<?> context) {
        return context.getOperation().getCacheNames().stream()
                .map(cacheManager::getCache)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    private LocalCache getOrCreateLocalCache(Cache cache) {
        return cacheMap.computeIfAbsent(
                cache.getName(),
                cacheName -> new LocalCache(cache, redisson, cacheEntryModifiedListener));
    }
}
