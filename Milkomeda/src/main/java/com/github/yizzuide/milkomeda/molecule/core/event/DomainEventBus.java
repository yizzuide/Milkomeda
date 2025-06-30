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

package com.github.yizzuide.milkomeda.molecule.core.event;

import com.github.yizzuide.milkomeda.molecule.core.agg.AggregateRoot;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

/**
 * The {@link DomainEventBus} serve for aggregate that collect events and publish from application layer.
 *
 * @since 4.0.0
 * @author yizzuide
 * Create at 2025/06/09 15:30
 */
public class DomainEventBus {

    private final DomainEventPublisher domainEventPublisher;

    private final ThreadLocal<Set<AggregateRoot>> HANGING_AGGREGATES = ThreadLocal.withInitial(HashSet::new);

    public DomainEventBus(DomainEventPublisher domainEventPublisher) {
        this.domainEventPublisher = domainEventPublisher;
    }

    /**
     * Get hanging aggregates
     * @param aggregateClazz  aggregate class
     * @return  aggregate events list
     * @param <T>   aggregate type
     */
    @SuppressWarnings("unchecked")
    public <T extends AggregateRoot> List<T> getHangingAggregates(Class<T> aggregateClazz) {
        if (hasHangingType(aggregateClazz)) {
            return (List<T>) HANGING_AGGREGATES.get().stream().toList();
        }
        return Collections.emptyList();
    }

    /**
     * Has hanging aggregate type.
     * @param aggregateClazz  aggregate class
     * @return  true if it has hanging type
     */
    public boolean hasHangingType(Class<? extends AggregateRoot> aggregateClazz) {
        return !HANGING_AGGREGATES.get().isEmpty() &&
                aggregateClazz.isAssignableFrom(HANGING_AGGREGATES.get().stream().findFirst().orElseThrow().getClass());
    }

    /**
     * Clear aggregate events.
     * @param consumer consumer aggregate
     */
    public void clear(Consumer<AggregateRoot> consumer) {
        Set<AggregateRoot> aggregateRoots = HANGING_AGGREGATES.get();
        if (aggregateRoots.isEmpty()) {
            return;
        }
        aggregateRoots.forEach(aggregate -> {
            if (consumer != null) {
                consumer.accept(aggregate);
            }
            aggregate.clearDomainEvents();
            aggregateRoots.remove(aggregate);
        });
        if (aggregateRoots.isEmpty()) {
            HANGING_AGGREGATES.remove();
        }
    }

    /**
     * Collect aggregate.
     * @param aggregate AggregateRoot
     */
    public void collect(AggregateRoot aggregate) {
        Set<AggregateRoot> aggregateRoots = HANGING_AGGREGATES.get();
        aggregateRoots.add(aggregate);
    }

    /**
     * Publish aggregate domain events.
     */
    public void publish() {
        clear(aggregateRoot -> aggregateRoot.domainEvents().forEach(domainEventPublisher::publishEvent));
    }

    /**
     * Publish an event object.
     * @param event any event
     */
    public void publishEvent(Object event) {
        domainEventPublisher.publishEvent(event);
    }
}
