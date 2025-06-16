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

package com.github.yizzuide.milkomeda.molecule.eventsourcing.postgresql.eventhandler;

import com.github.yizzuide.milkomeda.molecule.MoleculeContext;
import com.github.yizzuide.milkomeda.molecule.eventsourcing.postgresql.agg.Aggregate;
import com.github.yizzuide.milkomeda.molecule.eventsourcing.postgresql.event.Event;
import com.github.yizzuide.milkomeda.molecule.eventsourcing.postgresql.event.EventWithId;
import com.github.yizzuide.milkomeda.molecule.eventsourcing.postgresql.service.AggregateStore;

/**
 * The {@link AsyncEventHandler} means used to handle event after transaction commit.
 *
 * @since 4.0.0
 * @author yizzuide
 * Create at 2025/06/11 15:56
 */
@FunctionalInterface
public interface AsyncEventHandler {

    void handleEvent(EventWithId<Event> eventWithId);

    default String getSubscriptionName() {
        return getClass().getSimpleName();
    }

    /**
     * load aggregate with lasted domain event from the repository.
     * @param event             aggregate event
     * @param aggregateStore    aggregate repository
     * @param aggregateClazz    aggregate class
     * @return  aggregate
     * @param <T>   aggregate type
     */
    @SuppressWarnings("unchecked")
    default <T extends Aggregate> T loadAggregate(Event event, AggregateStore aggregateStore, Class<T> aggregateClazz) {
        return (T)aggregateStore.readAggregate(
                MoleculeContext.getAggregateTypeByClass(aggregateClazz), event.getAggregateId(), event.getVersion());
    }
}