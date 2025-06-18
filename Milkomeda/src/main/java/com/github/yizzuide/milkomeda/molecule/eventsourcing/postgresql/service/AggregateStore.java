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
import com.github.yizzuide.milkomeda.molecule.eventsourcing.postgresql.EventSourcingProperties;
import com.github.yizzuide.milkomeda.molecule.eventsourcing.postgresql.agg.Aggregate;
import com.github.yizzuide.milkomeda.molecule.eventsourcing.postgresql.agg.AggregateFactory;
import com.github.yizzuide.milkomeda.molecule.eventsourcing.postgresql.event.Event;
import com.github.yizzuide.milkomeda.molecule.eventsourcing.postgresql.event.EventWithId;
import com.github.yizzuide.milkomeda.molecule.eventsourcing.postgresql.exception.AggregateStateException;
import com.github.yizzuide.milkomeda.molecule.eventsourcing.postgresql.exception.OptimisticConcurrencyControlException;
import com.github.yizzuide.milkomeda.molecule.eventsourcing.postgresql.repository.AggregateRepository;
import com.github.yizzuide.milkomeda.molecule.eventsourcing.postgresql.repository.EventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.Nullable;
import org.springframework.lang.NonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * The {@link AggregateStore} is entry storage of {@link Aggregate} and {@link Event}.
 *
 * @since 4.0.0
 * @author yizzuide
 * Create at 2025/06/11 19:38
 */
@RequiredArgsConstructor
@Slf4j
public class AggregateStore {

    private final AggregateRepository aggregateRepository;
    private final EventRepository eventRepository;
    private final EventSourcingProperties properties;

    public Aggregate createAggregateFromType(@NonNull String aggregateType) {
        Long aggregateId = aggregateRepository.createAggregateIfAbsent(aggregateType, null);
        return AggregateFactory.newInstance(aggregateType, aggregateId);
    }

    public void deleteTempAggregate(@NonNull Long aggregateId) {
        aggregateRepository.deleteById(aggregateId);
    }

    public List<EventWithId<Event>> saveAggregate(Aggregate aggregate) {
        log.debug("Saving aggregate {}", aggregate);

        String aggregateType = MoleculeContext.getAggregateTypeByClass(aggregate.getClass());
        Long aggregateId = aggregate.getAggregateId();
        if (aggregateId == null) {
            throw new AggregateStateException("Aggregate id can't be null");
        }
        aggregateRepository.createAggregateIfAbsent(aggregateType, aggregateId);

        int expectedVersion = aggregate.getBaseVersion();
        int newVersion = aggregate.getVersion();
        if (!aggregateRepository.checkAndUpdateAggregateVersion(aggregateId, expectedVersion, newVersion)) {
            log.warn("Optimistic concurrency control error in aggregate {} {}: " +
                     "actual version doesn't match expected version {}",
                    aggregateType, aggregateId, expectedVersion);
            throw new OptimisticConcurrencyControlException(expectedVersion);
        }

        EventSourcingProperties.Snapshotting snapshotting = properties.getSnapshotting(aggregateType);
        List<Event> changes = aggregate.getChanges();
        List<EventWithId<Event>> newEvents = new ArrayList<>();
        for (Event event : changes) {
            log.info("Appending {} event: {}", aggregateType, event);
            EventWithId<Event> newEvent = eventRepository.appendEvent(event);
            newEvents.add(newEvent);
            createAggregateSnapshot(snapshotting, aggregate);
        }
        return newEvents;
    }

    private void createAggregateSnapshot(EventSourcingProperties.Snapshotting snapshotting,
                                         Aggregate aggregate) {
        if (snapshotting.isEnabled() && aggregate.getVersion() % snapshotting.getNthEvent() == 0) {
            log.info("Creating {} aggregate {} version {} snapshot",
                    MoleculeContext.getAggregateTypeByClass(aggregate.getClass()), aggregate.getAggregateId(), aggregate.getVersion());
            aggregateRepository.createAggregateSnapshot(aggregate);
        }
    }

    public Aggregate readAggregate(@NonNull String aggregateType,
                                   @NonNull Long aggregateId) {
        return readAggregate(aggregateType, aggregateId, null);
    }

    public Aggregate readAggregate(@NonNull String aggregateType,
                                   @NonNull Long aggregateId,
                                   @Nullable Integer version) {
        log.debug("Reading {} aggregate {}", aggregateType, aggregateId);
        EventSourcingProperties.Snapshotting snapshotting = properties.getSnapshotting(aggregateType);
        Aggregate aggregate;
        if (snapshotting.isEnabled()) {
            aggregate = readAggregateFromSnapshot(aggregateId, version)
                    .orElseGet(() -> {
                        log.debug("Aggregate {} snapshot not found", aggregateId);
                        return readAggregateFromEvents(aggregateType, aggregateId, version);
                    });

        } else {
            aggregate = readAggregateFromEvents(aggregateType, aggregateId, version);
        }
        log.debug("Read aggregate {}", aggregate);
        return aggregate;
    }

    private Optional<Aggregate> readAggregateFromSnapshot(Long aggregateId,
                                                          @Nullable Integer aggregateVersion) {
        return aggregateRepository.readAggregateSnapshot(aggregateId, aggregateVersion)
                .map(aggregate -> {
                    int snapshotVersion = aggregate.getVersion();
                    log.debug("Read aggregate {} snapshot version {}", aggregateId, snapshotVersion);
                    if (aggregateVersion == null || snapshotVersion < aggregateVersion) {
                        var events = eventRepository.readEvents(aggregateId, snapshotVersion, aggregateVersion)
                                .stream()
                                .map(EventWithId::event)
                                .toList();
                        log.debug("Read {} events after version {} for aggregate {}",
                                events.size(), snapshotVersion, aggregateId);
                        aggregate.loadFromHistory(events);
                    }
                    return aggregate;
                });
    }

    private Aggregate readAggregateFromEvents(String aggregateType,
                                              Long aggregateId,
                                              @Nullable Integer aggregateVersion) {
        var events = eventRepository.readEvents(aggregateId, null, aggregateVersion)
                .stream()
                .map(EventWithId::event)
                .toList();
        log.debug("Read {} events for aggregate {}", events.size(), aggregateId);
        Aggregate aggregate = AggregateFactory.newInstance(aggregateType, aggregateId);
        aggregate.loadFromHistory(events);
        return aggregate;
    }
}