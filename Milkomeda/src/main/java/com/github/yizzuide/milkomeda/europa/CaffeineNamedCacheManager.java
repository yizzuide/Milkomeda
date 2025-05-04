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

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.cache.caffeine.CaffeineCacheManager;

import java.util.Optional;

/**
 * The custom {@link CaffeineNamedCacheManager} create different caches based on the cache names.
 *
 * @since 4.0.0
 * @author yizzuide
 * Create at 2025/05/02 20:43
 */
@RequiredArgsConstructor
public class CaffeineNamedCacheManager extends CaffeineCacheManager {

    private EuropaProperties europaProperties;

    public CaffeineNamedCacheManager(EuropaProperties europaProperties) {
        this.europaProperties = europaProperties;
    }

    @NotNull
    @Override
    protected Cache<Object, Object> createNativeCaffeineCache(@NotNull String name) {
        Caffeine<Object, Object> caffeine = Caffeine.newBuilder();
        Optional<EuropaProperties.CacheProps> cachePropsOpt = europaProperties.getInstances()
                .keySet()
                .stream()
                .filter(key -> key.equals(name))
                .map(key -> europaProperties.getInstances().get(key))
                .findFirst();
        if (cachePropsOpt.isPresent()) {
            EuropaProperties.CacheProps cacheProps = cachePropsOpt.get();
            if (cacheProps.getL1MaxSize() > 0) {
                caffeine = Caffeine.newBuilder().maximumSize(cacheProps.getL1MaxSize());
            }
            if (cacheProps.getL1Ttl().getSeconds() > 0) {
                caffeine = caffeine.expireAfterWrite(cacheProps.getL1Ttl());
            }
            return caffeine.build();
        }
        return super.createNativeCaffeineCache(name);
    }
}
