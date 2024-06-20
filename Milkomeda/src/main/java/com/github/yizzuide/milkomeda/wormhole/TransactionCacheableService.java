/*
 * Copyright (c) 2024 yizzuide All rights Reserved.
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

package com.github.yizzuide.milkomeda.wormhole;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A cacheable transaction service for {@link TransactionService}.
 *
 * @param <R> the repository type
 *
 * @since 3.20.0
 * @author yizzuide
 * Create at 2024/04/07 18:58
 */
public abstract class TransactionCacheableService<R> implements TransactionService<R> {

    private static final Map<Class<?>, Object> CACHE_REPOSITORY_MAP = new ConcurrentHashMap<>();

    @SuppressWarnings("unchecked")
    @Override
    public <T> T getRepositoryProxy(Class<T> clazz) {
        T repository = (T) CACHE_REPOSITORY_MAP.get(clazz);
        if (repository == null) {
            repository = TransactionService.super.getRepositoryProxy(clazz);
            CACHE_REPOSITORY_MAP.put(clazz, repository);
        }
        return repository;
    }
}