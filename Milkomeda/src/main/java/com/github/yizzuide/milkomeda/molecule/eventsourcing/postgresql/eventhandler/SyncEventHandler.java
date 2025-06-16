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
import com.github.yizzuide.milkomeda.util.ReflectUtil;
import org.springframework.lang.NonNull;

import java.util.List;

/**
 * The {@link SyncEventHandler} means used to handle event at same transaction.
 *
 * @since 4.0.0
 * @author yizzuide
 * Create at 2025/06/11 15:48
 */
public interface SyncEventHandler {
    /**
     * Event type of handler dispatcher method and don't override this method.
     * @param events        aggregate events
     * @param aggregate     aggregate
     */
    default void handleEvents(List<EventWithId<Event>> events, Aggregate aggregate) {
        events.forEach(eventID -> {
            try {
                ReflectUtil.invokeDynamically(this, eventID.event(), "handleEvent");
            } catch (UnsupportedOperationException e) {
                handleEvent(eventID.event());
            }
        });
    }

    /**
     * The default handler for any impl of {@link Event}.
     * @param event Event
     */
    default void handleEvent(Event event) {}

    /**
     * load aggregate from event bus hanging.
     * @param aggregateId       aggregate id
     * @param aggregateClazz    aggregate class
     * @return  Aggregate
     * @param <T>   aggregate type
     */
    @SuppressWarnings("unchecked")
    default <T extends Aggregate> T loadAggregate(@NonNull Long aggregateId, Class<T> aggregateClazz) {
        List<Aggregate> aggregates = MoleculeContext.getDomainEventBus().getHangingAggregates(Aggregate.class);
        return (T) aggregates.stream().filter(agg -> agg.getAggregateId().equals(aggregateId)).findFirst().orElseThrow();
    }
}