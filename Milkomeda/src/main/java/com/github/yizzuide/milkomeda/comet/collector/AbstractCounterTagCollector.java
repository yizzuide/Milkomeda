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

package com.github.yizzuide.milkomeda.comet.collector;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import com.github.yizzuide.milkomeda.comet.core.CometData;
import com.github.yizzuide.milkomeda.comet.core.WebCometData;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.Async;

import javax.annotation.PreDestroy;
import java.time.Duration;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 * A counter tag collector for per request.
 *
 * @since 4.0.0
 * @author yizzuide
 * Create at 2025/05/30 14:23
 */
public abstract class AbstractCounterTagCollector extends TagCollector {

    private final LoadingCache<String, AtomicLong> localCounter;

    public AbstractCounterTagCollector(TaskScheduler taskScheduler) {
        localCounter = Caffeine.newBuilder()
                .expireAfterWrite(getPeriod().toMillis(), TimeUnit.MILLISECONDS)
                .build(key -> new AtomicLong(0));
        taskScheduler.scheduleAtFixedRate(this::flush, getPeriod());
    }

    @PreDestroy
    public void flush() {
        localCounter.asMap().forEach((key, atomic) -> {
            long delta = atomic.getAndSet(0);
            if (delta > 0) {
                onSave(key, delta);
            }
        });
    }

    @Async
    @Override
    public void prepare(CometData params) {
        String key = extractKey(((WebCometData) params));
        AtomicLong counter = localCounter.get(key);
        if (counter != null) {
            counter.incrementAndGet();
        }
    }

    @Override
    public void onSuccess(CometData params) {}

    @Override
    public void onFailure(CometData params) {}

    /**
     * Set period time at interval flush.
     * @return period time
     */
    protected abstract Duration getPeriod();

    /**
     * Extract key from {@link WebCometData}.
     * @param cometData WebCometData
     * @return  key
     */
    protected abstract String extractKey(WebCometData cometData);

    /**
     * Save the delta when flush is happened.
     * @param key   key
     * @param delta increment delta
     */
    protected abstract void onSave(String key, long delta);
}
