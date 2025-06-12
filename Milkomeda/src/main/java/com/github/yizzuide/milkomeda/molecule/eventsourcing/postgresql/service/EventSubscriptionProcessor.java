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

package com.github.yizzuide.milkomeda.molecule.eventsourcing.postgresql.service;

import com.github.yizzuide.milkomeda.molecule.MoleculeContext;
import com.github.yizzuide.milkomeda.molecule.eventsourcing.postgresql.event.Event;
import com.github.yizzuide.milkomeda.molecule.eventsourcing.postgresql.event.EventWithId;
import com.github.yizzuide.milkomeda.molecule.eventsourcing.postgresql.eventhandler.AsyncEventHandler;
import com.github.yizzuide.milkomeda.molecule.eventsourcing.postgresql.repository.EventRepository;
import com.github.yizzuide.milkomeda.molecule.eventsourcing.postgresql.repository.EventSubscriptionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * This processor use {@link AsyncEventHandler} to handle new events which find from {@link EventSubscriptionRepository}.
 *
 * @since 4.0.0
 * @author yizzuide
 * Create at 2025/06/11 19:28
 */
@Transactional(propagation = Propagation.REQUIRES_NEW)
@RequiredArgsConstructor
@Slf4j
public class EventSubscriptionProcessor {

    private final EventSubscriptionRepository subscriptionRepository;
    private final EventRepository eventRepository;

    @Async
    public void processNewEvents(AsyncEventHandler eventHandler) {
        String subscriptionName = eventHandler.getSubscriptionName();
        log.debug("Handling new events for subscription {}", subscriptionName);

        subscriptionRepository.createSubscriptionIfAbsent(subscriptionName);
        subscriptionRepository.readCheckpointAndLockSubscription(subscriptionName).ifPresentOrElse(
                checkpoint -> {
                    log.debug("Acquired lock on subscription {}, checkpoint = {}", subscriptionName, checkpoint);
                    List<EventWithId<Event>> events = eventRepository.readEventsAfterCheckpoint(
                            MoleculeContext.getAsyncEventHandlerTypeByClass(eventHandler.getClass()),
                            checkpoint.lastProcessedTransactionId(),
                            checkpoint.lastProcessedEventId()
                    );
                    log.debug("Fetched {} new event(s) for subscription {}", events.size(), subscriptionName);
                    if (!events.isEmpty()) {
                        events.forEach(eventHandler::handleEvent);
                        EventWithId<Event> lastEvent = events.getLast();
                        subscriptionRepository.updateEventSubscription(
                                subscriptionName, lastEvent.transactionId(), lastEvent.id());
                    }
                },
                () -> log.debug("Can't acquire lock on subscription {}", subscriptionName));
    }
}