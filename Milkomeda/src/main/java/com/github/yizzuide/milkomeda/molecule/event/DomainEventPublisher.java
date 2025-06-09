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

package com.github.yizzuide.milkomeda.molecule.event;

import com.github.yizzuide.milkomeda.molecule.agg.AggregateRoot;
import org.jetbrains.annotations.NotNull;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * The DomainEventPublisher used to collect aggregate register events and publish them.
 *
 * @since 4.0.0
 * @author yizzuide
 * Create at 2025/06/09 14:30
 */
public class DomainEventPublisher implements ApplicationEventPublisherAware {

    private ApplicationEventPublisher applicationEventPublisher;

    private final ThreadLocal<List<AggregateRoot>> AGGREGATE_EVENTS = ThreadLocal.withInitial(CopyOnWriteArrayList::new);

    @Override
    public void setApplicationEventPublisher(@NotNull ApplicationEventPublisher applicationEventPublisher) {
        this.applicationEventPublisher = applicationEventPublisher;
    }

    public void registerEvents(AggregateRoot aggregate) {
        List<AggregateRoot> aggregateRoots = AGGREGATE_EVENTS.get();
        if (aggregateRoots.isEmpty() || !aggregateRoots.contains(aggregate)) {
            aggregateRoots.add(aggregate);
        }
    }

    public void publishEvents() {
        List<AggregateRoot> aggregateRoots = AGGREGATE_EVENTS.get();
        if (aggregateRoots.isEmpty()) {
            return;
        }
        aggregateRoots.forEach(aggregate -> {
            aggregate.domainEvents().forEach(event -> applicationEventPublisher.publishEvent(event));
            aggregate.clearDomainEvents();
            aggregateRoots.remove(aggregate);
        });
        if (aggregateRoots.isEmpty()) {
            AGGREGATE_EVENTS.remove();
        }
    }
}
