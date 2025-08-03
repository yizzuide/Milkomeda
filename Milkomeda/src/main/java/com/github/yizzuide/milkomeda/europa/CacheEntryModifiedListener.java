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

import lombok.RequiredArgsConstructor;
import org.redisson.api.map.event.EntryEvent;
import org.redisson.api.map.event.EntryRemovedListener;
import org.redisson.api.map.event.EntryUpdatedListener;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;

/**
 * The {@link CacheEntryModifiedListener} listens for entries removed or updated from the distributed cache (Redis)
 * and ensures they are also removed from the local caches across nodes.
 *
 * @since 4.0.0
 * @author yizzuide
 * Create at 2025/05/01 17:35
 */
@RequiredArgsConstructor
public class CacheEntryModifiedListener implements EntryRemovedListener<Object, Object>, EntryUpdatedListener<Object, Object> {

    private final CacheManager cacheManager;

    private final EuropaProperties europaProperties;

    @Override
    public void onRemoved(EntryEvent event) {
        String cacheName = event.getSource().getName();
        EuropaProperties.CacheProps cacheProps = europaProperties.getInstances().get(cacheName);
        if (cacheProps.isOnlyCacheL2()) {
            return;
        }
        // 收到Redis缓存删除key后，把本地key删除
        Cache cache = cacheManager.getCache(cacheName);
        if (cache != null) {
            cache.evict(event.getKey());
        }
    }

    @Override
    public void onUpdated(EntryEvent event) {
        String cacheName = event.getSource().getName();
        EuropaProperties.CacheProps cacheProps = europaProperties.getInstances().get(cacheName);
        if (cacheProps.isOnlyCacheL2()) {
            return;
        }
        // 收到Redis缓存更新key后，把本地key更新
        Cache cache = cacheManager.getCache(cacheName);
        if (cache != null) {
            cache.put(event.getKey(), event.getValue());
        }
    }
}
