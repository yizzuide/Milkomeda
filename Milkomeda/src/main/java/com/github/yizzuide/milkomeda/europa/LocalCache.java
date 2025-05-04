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

import org.redisson.api.RMapCache;
import org.redisson.api.RedissonClient;
import org.springframework.cache.Cache;
import org.springframework.cache.support.SimpleValueWrapper;
import org.springframework.lang.NonNull;

import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

/**
 * This local cache implements {@link Cache} interface which invoke inner by Spring cache.
 *
 * @since 3.21.0
 * @author yizzuide
 * Create at 2025/05/01 17:33
 */
public class LocalCache implements Cache {

    private final Cache cache;
    private final RMapCache<Object, Object> distributedCache;

    public LocalCache(Cache cache, RedissonClient redisson, CacheEntryModifiedListener cacheEntryModifiedListener) {
        this.cache = cache;
        this.distributedCache = redisson.getMapCache(getName());
        this.distributedCache.addListener(cacheEntryModifiedListener);
    }

    @Override
    @NonNull
    public String getName() {
        return cache.getName();
    }

    @Override
    @NonNull
    public Object getNativeCache() {
        return cache.getNativeCache();
    }

    // To retrieve data, the system first checks the local cache for the key. If the key is not found, it queries the distributed cache.
    //  If the value exists in the distributed cache, it is also added to the local cache for faster subsequent access.
    @Override
    public ValueWrapper get(@NonNull Object key) {
        Object value = cache.get(key);
        if (value == null && (value = distributedCache.get(key)) != null) {
            cache.put(key, value);
        }
        return toValueWrapper(value);
    }

    private ValueWrapper toValueWrapper(Object value) {
        if (value == null) return null;
        return value instanceof ValueWrapper ? (ValueWrapper) value : new SimpleValueWrapper(value);
    }

    @Override
    public <T> T get(@NonNull Object key, Class<T> type) {
        return cache.get(key, type);
    }

    @Override
    public <T> T get(@NonNull Object key, @NonNull Callable<T> valueLoader) {
        return cache.get(key, valueLoader);
    }

    // When a method annotated with @Cacheable is executed, the put method is invoked.
    @Override
    public void put(@NonNull Object key, Object value) {
        distributedCache.put(key, value, EuropaProperties.getL2ExpirationTime(getName()).toMinutes(), TimeUnit.MINUTES);
        cache.put(key, value);
    }

    // When a cache eviction occurs (e.g., through a @CacheEvict annotation), the key is removed from the distributed cache.
    @Override
    public void evict(@NonNull Object key) {
        distributedCache.remove(key);
    }

    @Override
    public void clear() {
        cache.clear();
        distributedCache.clear();
        // 通知其它分布式服务清空缓存（目前不支持）
    }
}
